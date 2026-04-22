package com.ying.learneyjourney.Util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.CRC32;

//Agora token
public class AccessToken2 {

    public static final String VERSION = "007";

    private final String appId;
    private final String appCert;
    private final String channelName;
    private final int uid;
    private final int expire;
    private final ServiceRtc serviceRtc = new ServiceRtc();

    public AccessToken2(String appId, String appCert, String channelName, int uid, int expire) {
        this.appId = appId;
        this.appCert = appCert;
        this.channelName = channelName;
        this.uid = uid;
        this.expire = expire;
    }

    public ServiceRtc getServiceRtc() {
        return serviceRtc;
    }

    public String build() {
        try {
            ByteArrayOutputStream content = new ByteArrayOutputStream();

            packString(content, appId);
            packString(content, channelName);
            packUint32(content, uid);
            packUint32(content, expire);
            packUint16(content, (short) 1); // rtc service type
            serviceRtc.pack(content);

            byte[] signing = content.toByteArray();
            byte[] signature = hmacSha256(signing, appCert);

            ByteArrayOutputStream tokenContent = new ByteArrayOutputStream();
            packString(tokenContent, bytesToHex(signature));
            packUint32(tokenContent, crc32(channelName));
            packUint32(tokenContent, crc32(String.valueOf(uid)));
            tokenContent.write(signing);

            return VERSION + appId + base64Encode(tokenContent.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to build Agora token", e);
        }
    }

    public static class ServiceRtc {
        private final Map<Short, Integer> privileges = new TreeMap<>();

        public static final short PRIVILEGE_JOIN_CHANNEL = 1;
        public static final short PRIVILEGE_PUBLISH_AUDIO = 2;
        public static final short PRIVILEGE_PUBLISH_VIDEO = 3;
        public static final short PRIVILEGE_PUBLISH_DATA = 4;

        public void addPrivilege(short privilege, int expire) {
            privileges.put(privilege, expire);
        }

        public void pack(ByteArrayOutputStream out) throws Exception {
            packUint16(out, (short) privileges.size());
            for (Map.Entry<Short, Integer> entry : privileges.entrySet()) {
                packUint16(out, entry.getKey());
                packUint32(out, entry.getValue());
            }
        }
    }

    private static void packString(ByteArrayOutputStream out, String value) throws Exception {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        packUint16(out, (short) bytes.length);
        out.write(bytes);
    }

    private static void packUint16(ByteArrayOutputStream out, short value) throws Exception {
        out.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array());
    }

    private static void packUint32(ByteArrayOutputStream out, int value) throws Exception {
        out.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array());
    }

    private static int crc32(String value) {
        CRC32 crc = new CRC32();
        crc.update(value.getBytes(StandardCharsets.UTF_8));
        return (int) crc.getValue();
    }

    private static byte[] hmacSha256(byte[] message, String appCertificate) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(appCertificate.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        return mac.doFinal(message);
    }

    private static String base64Encode(byte[] data) {
        return java.util.Base64.getEncoder().encodeToString(data);
    }

    private static String bytesToHex(byte[] bytes) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}