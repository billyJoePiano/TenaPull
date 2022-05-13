package tenapull.client;

import org.apache.logging.log4j.*;

import javax.net.ssl.*;
import javax.ws.rs.client.*;
import java.security.*;
import java.security.cert.*;

/**
 * Static utility class for constructing a Jersey client that accepts any SSL certificate
 */
public class AcceptAnySSL {
    private static Logger logger = LogManager.getLogger(AcceptAnySSL.class);

    /**
     * Static utility class.  Can't be instantiated
     */
    private AcceptAnySSL() { }

    /**
     * Make Jersey client which accepts any SSL certificate.
     *
     * @return the client
     */
    public static Client makeClient() {
        return ClientBuilder.newBuilder()
                            .hostnameVerifier(makeHostnameVerifier())
                            .sslContext(makeSSLContext())
                            .build();
    }


    /**
     * Creates SSL context that trusts all certificates<br>
     * <br>
     * See resources:<br>
     * https://stackoverflow.com/questions/875467/java-client-certificates-over-https-ssl<br>
     * http://useof.org/java-open-source/org.glassfish.jersey.SslConfigurator<br>
     * https://www.techieshah.com/2019/08/jersey-client-how-to-skip-ssl.html<br>
     * https://stackoverflow.com/questions/6047996/ignore-self-signed-ssl-cert-using-jersey-client<br>
     * https://stackoverflow.com/questions/12060250/ignore-ssl-certificate-errors-with-java/42823950<br>
     * <br>
     * IMPORT TRUSTED SSL:<br>
     * https://stackoverflow.com/questions/6659360/how-to-solve-javax-net-ssl-sslhandshakeexception-error/6742204#6742204
     *
     * @return the ssl context
     */
    public static SSLContext makeSSLContext() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
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


        return sc;
    }

    /**
     * Make hostname verifier that ignores differences between given hostname
     * and certificate hostname
     *
     * @return the hostname verifier
     */
    public static HostnameVerifier makeHostnameVerifier() {
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        return hv;
    }
}
