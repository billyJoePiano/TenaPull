package nessusTools.client;

import org.apache.logging.log4j.*;

import javax.net.ssl.*;
import javax.ws.rs.client.*;
import java.security.*;
import java.security.cert.*;

public class AcceptAnySSL {
    private static Logger logger = LogManager.getLogger(AcceptAnySSL.class);

    //Utility class, no instances, only static methods
    private AcceptAnySSL() { }

    public static Client makeClient() {
        return ClientBuilder.newBuilder()
                            .hostnameVerifier(makeHostnameVerifier())
                            .sslContext(makeSSLContext())
                            .build();
    }


    // https://stackoverflow.com/questions/875467/java-client-certificates-over-https-ssl
    // http://useof.org/java-open-source/org.glassfish.jersey.SslConfigurator
    // https://www.techieshah.com/2019/08/jersey-client-how-to-skip-ssl.html
    // https://stackoverflow.com/questions/6047996/ignore-self-signed-ssl-cert-using-jersey-client
    // https://stackoverflow.com/questions/12060250/ignore-ssl-certificate-errors-with-java/42823950

    // IMPORT TRUSTED SSL:
    // https://stackoverflow.com/questions/6659360/how-to-solve-javax-net-ssl-sslhandshakeexception-error/6742204#6742204

    public static SSLContext makeSSLContext() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                        //return new X509Certificate[0];
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }};

        // Install the all-trusting trust manager

        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");

        } catch (NoSuchAlgorithmException e) {
            logger.error(e);
            return null;
        }

        try {
            sc.init(null, trustAllCerts, null);
        } catch (KeyManagementException e) {
            logger.error(e);
            return null;
        }

        //HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        return sc;
    }

    public static HostnameVerifier makeHostnameVerifier() {
        // Ignore differences between given hostname and certificate hostname
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        //HttpsURLConnection.setDefaultHostnameVerifier(hv);

        return hv;
    }
}
