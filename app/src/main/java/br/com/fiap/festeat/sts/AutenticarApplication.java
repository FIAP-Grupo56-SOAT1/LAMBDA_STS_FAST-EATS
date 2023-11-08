package br.com.fiap.festeat.sts;

import br.com.fiap.festeat.sts.request.ClienteRequest;
import br.com.fiap.festeat.sts.service.AutenticarService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.function.context.FunctionRegistration;
import org.springframework.cloud.function.context.FunctionType;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

import java.util.function.Function;

@SpringBootApplication
public class AutenticarApplication implements ApplicationContextInitializer<GenericApplicationContext> {

    public static void main(String[] args) {
        SpringApplication.run(AutenticarApplication.class, args);
    }

    @Override
    public void initialize(GenericApplicationContext context) {
        context.registerBean("autenticar", FunctionRegistration.class,
                () -> new FunctionRegistration<Function<ClienteRequest, String>>(new AutenticarService())
                        .type(FunctionType.from(ClienteRequest.class).to(String.class).getType()));
    }


}

