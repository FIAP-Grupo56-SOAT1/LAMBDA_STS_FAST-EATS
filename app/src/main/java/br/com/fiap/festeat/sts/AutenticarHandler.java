package br.com.fiap.festeat.sts;

import br.com.fiap.festeat.sts.request.ClienteRequest;
import br.com.fiap.festeat.sts.request.UserLoginRequestPayload;
import br.com.fiap.festeat.sts.response.ClienteResponse;
import br.com.fiap.festeat.sts.response.UserLoginResponsePayload;
import br.com.fiap.festeat.sts.service.AutenticarService;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class AutenticarHandler implements RequestHandler<ClienteRequest, String> {

    static Logger logger = Logger.getLogger(AutenticarHandler.class.getName());

    private final String userPoolId = System.getenv("USER_POOL_ID");
    private final String clientId = System.getenv("CLIENTE_ID");
    private final String region = System.getenv("AWS_REGION");
    private final String accessKey = System.getenv("ACCESS_KEY");
    private final String secretKey = System.getenv("SECRET_KEY");
    private final String userCognito = System.getenv("USER_COGNITO");
    private final String passwordCognito = System.getenv("PASSWORD_COGNITO");

    private final String nlb = System.getenv("NLB_API");
    private final String sessionToken = System.getenv("SESSION_TOKEN");
    private final ObjectMapper mapper = new ObjectMapper();
    private final AutenticarService autenticar = new AutenticarService();

    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    @Override
    public String handleRequest(ClienteRequest cliente, Context context) {
        logger.info("entrou no metodo handler:" + cliente.toString());
        try {

            if (autenticar.cpfNaoValido(cliente)) {
                return "CPF INVALIDO";
            }

            if (validarCadastroCliente(cliente)) {
                UserLoginResponsePayload userLoginResponsePayload = gerarToken();

                return userLoginResponsePayload.getAccessToken();
            } else {
                return "CPF NAO CADASTRADO";
            }
        } catch (Exception exception) {
            logger.info(exception.getMessage() + exception.getCause());
            return "ERRO NA VALIDACAO DO CPF";
        }
    }


    public UserLoginResponsePayload gerarToken() throws Exception {
        UserLoginRequestPayload userLoginRequestPayload = new UserLoginRequestPayload(userCognito, passwordCognito);
        //BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
        BasicSessionCredentials awsCreds = new BasicSessionCredentials(accessKey, secretKey, sessionToken);

        AWSCognitoIdentityProvider cognitoClient = AWSCognitoIdentityProviderClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds)).withRegion(region).build();

        UserLoginResponsePayload userLoginResponsePayload = new UserLoginResponsePayload();

        final Map<String, String> authParams = new HashMap<>();
        authParams.put("USERNAME", userLoginRequestPayload.getUserName());
        authParams.put("PASSWORD", userLoginRequestPayload.getPassword());

        final AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest();
        authRequest.withAuthFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH).withClientId(clientId)
                .withUserPoolId(userPoolId).withAuthParameters(authParams);
        logger.info("DADOS PARA REQUEST: "+authRequest.toString());

        try {

            AdminInitiateAuthResult result = cognitoClient.adminInitiateAuth(authRequest);

            AuthenticationResultType authenticationResult = null;

            if (result.getChallengeName() != null && !result.getChallengeName().isEmpty()) {

                logger.info("Challenge Name is " + result.getChallengeName());

                if (result.getChallengeName().contentEquals("NEW_PASSWORD_REQUIRED")) {
                    if (userLoginRequestPayload.getPassword() == null) {
                        throw new Exception("User must change password " + result.getChallengeName());

                    } else {

                        final Map<String, String> challengeResponses = new HashMap<>();
                        challengeResponses.put("USERNAME", userLoginRequestPayload.getUserName());
                        challengeResponses.put("PASSWORD", userLoginRequestPayload.getPassword());
                        // add new password
                        challengeResponses.put("NEW_PASSWORD", userLoginRequestPayload.getNewPassword());

                        final AdminRespondToAuthChallengeRequest request = new AdminRespondToAuthChallengeRequest()
                                .withChallengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
                                .withChallengeResponses(challengeResponses).withClientId(clientId)
                                .withUserPoolId(userPoolId).withSession(result.getSession());

                        AdminRespondToAuthChallengeResult resultChallenge = cognitoClient
                                .adminRespondToAuthChallenge(request);
                        authenticationResult = resultChallenge.getAuthenticationResult();

                        userLoginResponsePayload.setAccessToken(authenticationResult.getAccessToken());
                        userLoginResponsePayload.setRefreshToken(authenticationResult.getRefreshToken());
                    }

                } else {
                    throw new Exception("User has other challenge " + result.getChallengeName());
                }

                cognitoClient.shutdown();
                return userLoginResponsePayload;
            } else {

                logger.info("User has no challenge");
                authenticationResult = result.getAuthenticationResult();

                userLoginResponsePayload.setAccessToken(authenticationResult.getAccessToken());
                userLoginResponsePayload.setRefreshToken(authenticationResult.getRefreshToken());
                cognitoClient.shutdown();

                return userLoginResponsePayload;
            }

        } catch (InvalidParameterException e) {
            cognitoClient.shutdown();
            throw new Exception(e.getErrorMessage());
        } catch (Exception e) {
            cognitoClient.shutdown();
            throw new Exception(e.getMessage());
        }

    }

    private boolean validarCadastroCliente(ClienteRequest cliente) throws Exception {
        String rotaClientes = "/clientes/"+cliente.getCpf();
        HttpGet request = new HttpGet(nlb+rotaClientes);

        try (CloseableHttpResponse response = httpClient.execute(request)) {

            // Get HttpResponse Status
            System.out.println(response.getStatusLine().toString());

            HttpEntity entity = response.getEntity();
            Header headers = entity.getContentType();
            logger.info("Headers retorno:"+headers.toString());

            try {
                String result = EntityUtils.toString(entity);
                logger.info("Resultado consulta GET:"+result);
                ClienteResponse clienteResponse = mapper.readValue(result, ClienteResponse.class);
                if (!clienteResponse.getCpf().isEmpty()){
                    return true;
                }

            }catch (Exception exception){
                logger.info(" ERRO AO CONSULTAR CLIENTE: "+exception.getMessage() + exception.getCause());
            }



        }

        return false;
    }
}