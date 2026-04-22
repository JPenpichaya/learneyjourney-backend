package com.ying.learneyjourney.Util;
//Agora Token Builder
public class RtcTokenBuilder2 {

    public enum Role {
        ROLE_PUBLISHER,
        ROLE_SUBSCRIBER
    }

    public static String buildTokenWithUid(
            String appId,
            String appCertificate,
            String channelName,
            int uid,
            Role role,
            int privilegeExpireTs
    ) {
        AccessToken2 token = new AccessToken2(
                appId,
                appCertificate,
                channelName,
                uid,
                privilegeExpireTs
        );

        token.getServiceRtc().addPrivilege(
                AccessToken2.ServiceRtc.PRIVILEGE_JOIN_CHANNEL,
                privilegeExpireTs
        );

        if (role == Role.ROLE_PUBLISHER) {
            token.getServiceRtc().addPrivilege(
                    AccessToken2.ServiceRtc.PRIVILEGE_PUBLISH_AUDIO,
                    privilegeExpireTs
            );
            token.getServiceRtc().addPrivilege(
                    AccessToken2.ServiceRtc.PRIVILEGE_PUBLISH_VIDEO,
                    privilegeExpireTs
            );
            token.getServiceRtc().addPrivilege(
                    AccessToken2.ServiceRtc.PRIVILEGE_PUBLISH_DATA,
                    privilegeExpireTs
            );
        }

        return token.build();
    }
}