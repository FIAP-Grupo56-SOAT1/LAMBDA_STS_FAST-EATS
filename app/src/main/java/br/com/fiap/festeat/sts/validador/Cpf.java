package br.com.fiap.festeat.sts.validador;


public record Cpf(String valor) {
    public Cpf {
        String cpfFormatado = formatarCpf(valor);
        if (Boolean.FALSE.equals(validarCpf(cpfFormatado))) throw new RuntimeException("CPF inválido");
        valor = cpfFormatado;
    }

    private String formatarCpf(String cpf) {
        return cpf.replaceAll("\\D", "");
    }

    private Boolean validarCpf(String cpf) {
        if (cpf.length() != 11) return false;
        if (cpf.matches("(\\d)\\1{10}")) return false;
        int digito1 = calcularDigitoVerificador(cpf.substring(0, 9), 10);
        int digito2 = calcularDigitoVerificador(cpf.substring(0, 9) + digito1, 11);
        return digito1 == Character.getNumericValue(cpf.charAt(9)) && digito2 == Character.getNumericValue(cpf.charAt(10));
    }

    private int calcularDigitoVerificador(String cpf, int peso) {
        int soma = 0;
        for (int i = 0; i < cpf.length(); i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * peso--;
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
