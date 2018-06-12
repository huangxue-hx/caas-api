package com.harmonycloud.service.platform.socket.term;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.socket.term.helper.IOHelper;
import com.harmonycloud.service.platform.socket.term.helper.ThreadHelper;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.pty4j.PtyProcess;
import com.pty4j.WinSize;
import com.sun.jna.Platform;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;

@Component
@Scope("prototype")
public class TerminalService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TerminalService.class);

    @Value("${shell:#{null}}")
    private String shellStarter;

    @Autowired
    ClusterService clusterService;
    @Autowired
    NamespaceLocalService namespaceLocalService;

    private boolean isReady;
    private String[] termCommand;
    private PtyProcess process;
    private Integer columns = 20;
    private Integer rows = 10;
    private BufferedReader inputReader;
    private BufferedReader errorReader;
    private BufferedWriter outputWriter;
    private WebSocketSession webSocketSession;

    private LinkedBlockingQueue<String> commandQueue = new LinkedBlockingQueue<>();

    public void onTerminalInit() {

    }

    public void onTerminalReady(String container,String pod,String namespace, String clusterId, String scriptType) {

        ThreadHelper.start(() -> {
            isReady = true;
            try {
                initializeProcess(container,pod,namespace,clusterId,scriptType);
            } catch (Exception e) {
                LOGGER.error("服务web控制台初始化失败,namespace:{},pod:{}",new String[]{namespace,pod},e);
            }
        });

    }

    private void initializeProcess(String container,String pod,String namespace,String clusterId,String scriptType) throws Exception {
        Cluster cluster = null;
        if(StringUtils.isNotBlank(namespace) && !CommonConstant.KUBE_SYSTEM.equalsIgnoreCase(namespace)){
            cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        }else if(StringUtils.isNotBlank(clusterId)){
            cluster = clusterService.findClusterById(clusterId);
        }
        if(cluster == null){
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        String userHome = System.getProperty("user.home");
        /*Path dataDir = Paths.get(userHome).resolve(".terminalfx");
        IOHelper.copyLibPty(dataDir);*/

        String libPath = getJarContainingFolderPath(TerminalService.class);
        if(libPath.endsWith("lib")){
            Path ptyLibDir = Paths.get(libPath);
            IOHelper.copyLibPty(ptyLibDir);
        }

        if (Platform.isWindows()) {
            this.termCommand = "cmd.exe".split("\\s+");
        } else {
            String command = "/usr/bin/kubectl exec "+pod+" --container="+container+" -it "+scriptType+" -n "+namespace
                    +" --server="+cluster.getApiServerUrl()+" --token="+cluster.getMachineToken()+" --insecure-skip-tls-verify=true";
            LOGGER.info("linux shell command:{}",command);
            this.termCommand = command.split("\\s+");
        }

        if(Objects.nonNull(shellStarter)){
            this.termCommand = shellStarter.split("\\s+");
        }

        Map<String, String> envs = new HashMap<>(System.getenv());
        envs.put("TERM", "xterm");
        //System.setProperty("PTY_LIB_FOLDER", dataDir.resolve("libpty").toString());
        LOGGER.info("pty4j lib dir:{}",System.getProperty("PTY_LIB_FOLDER"));
        this.process = PtyProcess.exec(termCommand, envs, userHome);
        process.setWinSize(new WinSize(columns, rows));
        this.inputReader = new BufferedReader(new InputStreamReader(process.getInputStream(),"UTF-8"));
        this.errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(),"UTF-8"));
        this.outputWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(),"UTF-8"));

        ThreadHelper.start(() -> {
            printReader(inputReader);
        });

        ThreadHelper.start(() -> {
            printReader(errorReader);
        });

        process.waitFor();

    }

    public void print(String text) throws IOException {

        Map<String, String> map = new HashMap<>();
        map.put("type", "TERMINAL_PRINT");
        map.put("text", text);

        String message = new ObjectMapper().writeValueAsString(map);

        webSocketSession.sendMessage(new TextMessage(message));

    }

    private void printReader(BufferedReader bufferedReader) {
        try {
            int nRead;
            char[] data = new char[1 * 1024];

            while ((nRead = bufferedReader.read(data, 0, data.length)) != -1) {
                StringBuilder builder = new StringBuilder(nRead);
                builder.append(data, 0, nRead);
                print(builder.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onCommand(String command) throws InterruptedException {

        if (Objects.isNull(command)) {
            return;
        }

        commandQueue.put(command);
        ThreadHelper.start(() -> {
            try {
                outputWriter.write(commandQueue.poll());
                outputWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    public void onTerminalResize(String columns, String rows) {
        if (Objects.nonNull(columns) && Objects.nonNull(rows)) {
            this.columns = Integer.valueOf(columns);
            this.rows = Integer.valueOf(rows);

            if (Objects.nonNull(process)) {
                process.setWinSize(new WinSize(this.columns, this.rows));
            }

        }
    }

    public void setWebSocketSession(WebSocketSession webSocketSession) {
        this.webSocketSession = webSocketSession;
    }

    public WebSocketSession getWebSocketSession() {
        return webSocketSession;
    }

    public String getJarContainingFolderPath(Class aclass) throws Exception {
        CodeSource codeSource = aclass.getProtectionDomain().getCodeSource();
        File jarFile;
        if (codeSource.getLocation() != null) {
            jarFile = new File(codeSource.getLocation().toURI());
        } else {
            String path = aclass.getResource(aclass.getSimpleName() + ".class").getPath();
            int startIndex = path.indexOf(":") + 1;
            int endIndex = path.indexOf("!");
            if (startIndex == -1 || endIndex == -1) {
                throw new IllegalStateException("Class " + aclass.getSimpleName() + " is located not within a jar: " + path);
            }

            String jarFilePath = path.substring(startIndex, endIndex);
            jarFilePath = (new URI(jarFilePath)).getPath();
            jarFile = new File(jarFilePath);
        }

        return jarFile.getParentFile().getAbsolutePath();
    }
}