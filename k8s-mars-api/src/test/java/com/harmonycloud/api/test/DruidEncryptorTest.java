package com.harmonycloud.api.test;

import com.alibaba.druid.filter.config.ConfigTools;
import org.junit.Test;


/**
 * alibaba druid
 *
 * @author hepeng
 * @version V1.0
 * @since 2018-10-10 09:53
 */
public class DruidEncryptorTest {

    @Test
    public void testEncrypt() throws Exception {
        String[] args = new String[]{"123456"};
        String password = args[0];
        String[] arr = ConfigTools.genKeyPair(512);
        System.out.println("privateKey:" + arr[0]);
        String privateKey = "MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEAhHdoIuLPQGrw5tlSwE0SbbVCpElNppFdxN3BBot0aa/4UdjTOG6+M5zEFgMrQdrb06WBgLESy0zZSXFw8z0KsQIDAQABAkBgS1EHMyuH33WZyteN9Tj1SXPVa3goIrowdydvc4a/oFsyTlgVaFLXy2KHPgNcWa31M+K/BrJoR8eIZAPNw0FBAiEA46t4tWH7kdLmwm+Rh0Tf33NHT/hI7wf2ihED6Gj7EKkCIQCU8y/jGH5hlJSSFkvXCONO9nZHI+dGYR/P67ZnFJEGyQIgdTd9iHWQHn4lTQpANRLi6JrjpmrAskC5UFB+YJRcXzECIQCReP3xFy//j8SW9S2249hVe4LDMj6jzyHpyPXJsR1/eQIhAKEGi7GpDYQABVsz+kZ1tNAapZELSPOu2hAf5JGdsVVr";
        String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAIR3aCLiz0Bq8ObZUsBNEm21QqRJTaaRXcTdwQaLdGmv+FHY0zhuvjOcxBYDK0Ha29OlgYCxEstM2UlxcPM9CrECAwEAAQ==";
        System.out.println("publicKey:" + arr[1]);
        System.out.println("123456:" + ConfigTools.encrypt(arr[0], password));
        System.out.println("123 :" + ConfigTools.encrypt(arr[0], "123"));
        System.out.println("admin :" + ConfigTools.encrypt(arr[0], "admin"));
        String result = ConfigTools.decrypt(arr[1],ConfigTools.encrypt(arr[0], password));
        System.out.println(result);
    }
}