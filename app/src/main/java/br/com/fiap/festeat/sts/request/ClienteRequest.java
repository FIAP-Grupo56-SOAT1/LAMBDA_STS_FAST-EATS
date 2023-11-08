package br.com.fiap.festeat.sts.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClienteRequest {

    @JsonProperty("cpf")
    private String cpf;

    public ClienteRequest() {}

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    @Override
    public String toString() {
        return "{" + "\"cpf\":" +"\""+ cpf +"\""+"}";
    }
}