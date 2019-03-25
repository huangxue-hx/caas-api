package com.harmonycloud.common.util;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by dengyl on 2019-02-28
 * ip相关
 */
public class IpUtil {

    public static boolean isNotIpv4(String ip) {
        return !isIpv4(ip);
    }

    /**
     * 是否ipv4
     *
     * @param ip ip地址 192.168.1.1
     * @return
     */
    public static boolean isIpv4(String ip) {
        String[] addr = ip.split("\\.");
        if (addr.length != 4) {
            return false;
        }
        for (String f : addr) {
            if (!isPositive(f)) {
                return false;
            }
            int n = Integer.parseInt(f);
            if (n < 0 || n > 255) {
                return false;
            }
        }

        return true;
    }


    /**
     * 是否cidr格式
     *
     * @param cidr cidr 192.168.1.1/24
     * @return
     */
    public static boolean isCidr(String cidr) {
        String[] cidrs = cidr.split("/");
        if (cidrs.length != 2) {
            return false;
        }
        if (!isIpv4(cidrs[0])) {
            return false;
        }
        if (!isPositive(cidrs[1])) {
            return false;
        }

        int i = Integer.parseInt(cidrs[1]);

        return i >= 0 && i <= 32 && (i != 0 || "0.0.0.0".equals(cidrs[0]));
    }


    /**
     * 将ipv4转为int
     *
     * @param ip ip地址 192.168.1.1
     * @return
     */
    public static Integer ipv4ToInt(String ip) {
        if (!isIpv4(ip)) {
            return null;
        }

        String[] ips = ip.split("\\.");
        int ipv4 = 0;

        // 因为每个位置最大255，刚好在2进制里表示8位
        for (String ip4 : ips) {
            if (StringUtils.isBlank(ip4)) {
                return null;
            }
            // 这里应该用+也可以,但是位运算更快
            ipv4 = (ipv4 << 8) | Integer.parseInt(ip4);
        }

        return ipv4;
    }


    /**
     * int转回ipv4
     *
     * @param ip ipv4的int值
     * @return
     */
    public static String intToIpv4(int ip) {
        // 思路很简单，每8位拿一次，就是对应位的IP
        StringBuilder sb = new StringBuilder();
        for (int i = 3; i >= 0; i--) {
            int ipa = (ip >> (8 * i)) & (0xff);
            sb.append(ipa).append(".");
        }
        sb.delete(sb.length() - 1, sb.length());

        return sb.toString();
    }


    /**
     * 简单校验
     *
     * @param val
     * @return
     */
    private static boolean isPositive(String val) {
        if (StringUtils.isBlank(val)) {
            return false;
        }

        int n = val.length();
        if (n > 3) {    // 此处可以不加限制，即127.0.0.00001也是正确ip
            return false;
        }
        for (int i = 0; i < n; i++) {
            if (!Character.isDigit(val.charAt(i))) {
                return false;
            }
        }
        return true;
    }


    /**
     * 根据掩码位数获取掩码
     */
    public static String getNetMask(String mask) {
        if (StringUtils.isBlank(mask)) {
            return null;
        }
        int netMask = Integer.parseInt(mask);
        if (netMask > 32 || netMask < 0) {
            return null;
        }

        // 子网掩码为1占了几个字节
        int num1 = netMask / 8;
        // 子网掩码的补位位数
        int num2 = netMask % 8;
        int[] array = new int[4];
        for (int i = 0; i < num1; i++) {
            array[i] = 255;
        }
        for (int i = num1; i < 4; i++) {
            array[i] = 0;
        }
        for (int i = 0; i < num2; num2--) {
            array[num1] += Math.pow(2, 8 - num2);
        }
        return array[0] + "." + array[1] + "." + array[2] + "." + array[3];
    }


    /**
     * 根据网段计算起始IP 网段格式:x.x.x.x/x
     * 一个网段0一般为网络地址，255一般为广播地址
     * 起始IP计算：网段与掩码相与之后加一的IP地址
     *
     * @param cidr 网段
     * @return 起始IP
     */
    public static String getStartIp(String cidr) {
        if (cidr == null || !isCidr(cidr)) {
            return null;
        }

        StringBuilder startIp = new StringBuilder();
        String[] arr = cidr.split("/");
        String ip = arr[0];
        String maskIndex = arr[1];
        String mask = getNetMask(maskIndex);
        if (4 != ip.split("\\.").length || mask == null) {
            return null;
        }
        int[] ipArray = new int[4];
        int[] netMaskArray = new int[4];
        for (int i = 0; i < 4; i++) {
            try {
                ipArray[i] = Integer.parseInt(ip.split("\\.")[i]);
                netMaskArray[i] = Integer.parseInt(mask.split("\\.")[i]);
                if (ipArray[i] > 255 || ipArray[i] < 0 || netMaskArray[i] > 255 || netMaskArray[i] < 0) {
                    return null;
                }
                ipArray[i] = ipArray[i] & netMaskArray[i];
                if (i == 3) {
                    startIp.append(ipArray[i] + 1);
                } else {
                    startIp.append(ipArray[i]).append(".");
                }
            } catch (NumberFormatException e) {
                System.out.println(e.getMessage());
            }
        }
        return startIp.toString();
    }


    /**
     * 根据网段计算结束IP
     *
     * @param cidr 网段 192.168.1.1/24
     * @return 结束IP
     */
    public static String getEndIp(String cidr) {
        if (cidr == null || !isCidr(cidr)) {
            return null;
        }

        StringBuilder endIp = new StringBuilder();
        String startIp = getStartIp(cidr);
        String[] arr = cidr.split("/");
        String maskIndex = arr[1];
        // 实际需要的IP个数
        int hostNumber = 0;
        int[] startIpArray = new int[4];
        try {
            hostNumber = 1 << 32 - (Integer.parseInt(maskIndex));
            for (int i = 0; i < 4; i++) {
                startIpArray[i] = Integer.parseInt(startIp.split("\\.")[i]);
                if (i == 3) {
                    startIpArray[i] = startIpArray[i] - 1;
                    break;
                }
            }
            startIpArray[3] = startIpArray[3] + (hostNumber - 1);
        } catch (NumberFormatException e) {
            System.out.println(e.getMessage());
        }

        if (startIpArray[3] > 255) {
            int k = startIpArray[3] / 256;
            startIpArray[3] = startIpArray[3] % 256;
            startIpArray[2] = startIpArray[2] + k;
        }
        if (startIpArray[2] > 255) {
            int j = startIpArray[2] / 256;
            startIpArray[2] = startIpArray[2] % 256;
            startIpArray[1] = startIpArray[1] + j;
            if (startIpArray[1] > 255) {
                int k = startIpArray[1] / 256;
                startIpArray[1] = startIpArray[1] % 256;
                startIpArray[0] = startIpArray[0] + k;
            }
        }
        for (int i = 0; i < 4; i++) {
            if (i == 3) {
                startIpArray[i] = startIpArray[i] - 1;
            }
            if ("".equals(endIp.toString()) || endIp.length() == 0) {
                endIp.append(startIpArray[i]);
            } else {
                endIp.append(".").append(startIpArray[i]);
            }
        }
        return endIp.toString();
    }

    /**
     * 比较两个ip，第一个是否比第二个小
     *
     * @param startIp 开始ip
     * @param endIp   结束ip
     * @return
     */
    public static boolean isLessThan(String startIp, String endIp) {
        return ipv4ToLong(startIp) < ipv4ToLong(endIp);
    }

    /**
     * ip转成long值
     *
     * @param ip ip：192.168.1.1
     * @return
     */
    public static long ipv4ToLong(String ip) {
        // 分离出ip中的四个数字位
        String[] iPArr = ip.split("\\.");

        // 取得各个数字
        long[] ipNum = new long[4];
        for (int i = 0; i < 4; i++) {
            ipNum[i] = Long.parseLong(iPArr[i]);
        }

        // 各个数字乘以相应的数量级
        return ipNum[0] * 256 * 256 * 256 + ipNum[1] * 256 * 256 + ipNum[2] * 256 + ipNum[3];
    }


    public static void main(String[] args) {
        String ip1 = "192/168.1.1/24";
        String ip2 = "192.168.1.1/33";
        String ip3 = "192.168.1.1/24";

        System.out.println(ip1 + "：" + isCidr(ip1));
        System.out.println(ip2 + "：" + isCidr(ip2));
        System.out.println(ip3 + "：" + isCidr(ip3));

        String ip4 = "192.168.1.1";
        Integer ip5 = ipv4ToInt(ip4);
        System.out.println(ip5 + "：" + intToIpv4(ip5));
        ip4 = "192.168.255.1";
        ip5 = ipv4ToInt(ip4);
        System.out.println(ip5 + "：" + intToIpv4(ip5));

        System.out.println(getStartIp(ip3));
        System.out.println(getEndIp(ip3));

        String ip6 = "192.168.23.35/21";
        System.out.println(getNetMask(ip6.split("/")[1]));
        System.out.println(getStartIp(ip6));
        System.out.println(getEndIp(ip6));

        System.out.println(isLessThan("192.168.23.1", "192.168.23.35"));
    }

}
