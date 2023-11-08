package br.com.fiap.festeat.sts;

import br.com.fiap.festeat.sts.request.ClienteRequest;
import br.com.fiap.festeat.sts.request.UserLoginRequestPayload;
import br.com.fiap.festeat.sts.response.UserLoginResponsePayload;
import br.com.fiap.festeat.sts.service.AutenticarService;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.*;
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
    private final ObjectMapper mapper = new ObjectMapper();
    private final AutenticarService autenticar = new AutenticarService();

    @Override
    public String handleRequest(ClienteRequest cliente, Context context) {
        logger.info("entrou no metodo handler:" + cliente.toString());
        try {

            if (autenticar.cpfNaoValido(cliente)) {
                return "CPF INVALIDO";
            }

            if (validarClienteCadastrado(cliente)) {
                UserLoginResponsePayload userLoginResponsePayload = gerarToken();

                return mapper.writeValueAsString(userLoginResponsePayload);
            } else {
                return "CPF NAO CADASTRADO";
            }
        } catch (Exception exception) {
            logger.info(exception.getMessage() + exception.getCause());
            return "ERRO NA VALIDACAO DO CPF";
        }
    }

    private boolean validarClienteCadastrado(ClienteRequest cliente) {
        // UPDATE the HOST string to match your RDS endpoint
        String HOST = System.getenv("containerDbServer");
        String DB_NAME = System.getenv("containerDbName");
        String DB_PORTA = System.getenv("containerDbPort");
        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DB_URL = String.format("jdbc:mysql://%s:%s/%s", HOST,DB_PORTA,DB_NAME);
        String USER = System.getenv("containerDbUser");;
        String SECRET_NAME = System.getenv("containerDbServer");;
        String PASSWORD_DB = System.getenv("containerDbPassword");;
        logger.info("URL_DB:"+DB_URL);

        try {

            Class.forName(JDBC_DRIVER);
            Connection connection = DriverManager.getConnection(DB_URL,USER,PASSWORD_DB);
            Statement statement = null;
            ResultSet resultSet = null;
            String cpf = "";
            String nome = "";
            String email = "";

            // Run a simple query to test the connection
            try (Statement stmt = connection.createStatement(); ) {
                try( ResultSet rs = stmt.executeQuery("SELECT * FROM clientes C WHERE C.cpf = '"
                        +cliente.getCpf() + "'")) {
                    logger.info("resultset:"+rs.toString());
                    while(rs.next()) {
                        cpf = rs.getString("cpf");
                        nome = rs.getString("nome");
                        email = rs.getString("email");
                    }
                    resultSet = rs;
                    statement =  stmt;
                } catch(Exception e) {
                    logger.info(e.getMessage()+e.getCause());
                }
            } catch (Exception e) {
                logger.info(e.getMessage()+e.getCause());

            }finally {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            }
            logger.info("DADOS DA CONSULTA: cpf:"+cpf+" nome:"+nome+" email:"+email);
            if(cpf.isEmpty()){
                return false;
            }
            logger.info("Cliente encontrado na base, segue para gerar token: cpf"+
                    cpf+" nome:"+nome+" email:"+email);
            return true;
        } catch (Exception exception) {
            logger.info(exception.getMessage() + exception.getCause());
            return false;
        }

    }

    public UserLoginResponsePayload gerarToken() throws Exception {
        UserLoginRequestPayload userLoginRequestPayload = new UserLoginRequestPayload(userCognito, passwordCognito);
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);

        AWSCognitoIdentityProvider cognitoClient = AWSCognitoIdentityProviderClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds)).withRegion(region).build();

        UserLoginResponsePayload userLoginResponsePayload = new UserLoginResponsePayload();

        final Map<String, String> authParams = new HashMap<>();
        authParams.put("USERNAME", userLoginRequestPayload.getUserName());
        authParams.put("PASSWORD", userLoginRequestPayload.getPassword());

        final AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest();
        authRequest.withAuthFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH).withClientId(clientId)
                .withUserPoolId(userPoolId).withAuthParameters(authParams);

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
}