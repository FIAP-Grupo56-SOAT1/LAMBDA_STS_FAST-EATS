package br.com.fiap.festeat.sts.service;


import br.com.fiap.festeat.sts.AutenticarHandler;
import br.com.fiap.festeat.sts.request.ClienteRequest;
import br.com.fiap.festeat.sts.validador.Cpf;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.function.Function;
import java.util.logging.Logger;

public class AutenticarService implements Function<ClienteRequest, String> {

    static Logger logger = Logger.getLogger(AutenticarService.class.getName());
    @Override
    public String apply(ClienteRequest cliente) {
        return "";
    }

    public boolean cpfNaoValido(ClienteRequest cliente) {
        try {
            Cpf cpfFormatado = new Cpf(cliente.getCpf());
            logger.info("CPF validado com sucesso: " + cpfFormatado.valor());
        } catch (Exception exception) {
            logger.info(exception.getMessage() + exception.getCause());
            return true;
        }
        return false;
    }
}