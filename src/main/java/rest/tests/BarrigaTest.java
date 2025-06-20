package rest.tests;

import core.BaseTest;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


public class BarrigaTest extends BaseTest {

    private String token;

    @Before
    public void login() {
        Map<String, String> login = new HashMap<>();
        login.put("email", "wagner@aquino");
        login.put("senha", "123456");

        token = given()
                .body(login)
                .when()
                .post("/signin")
                .then()
                .statusCode(200)
                .extract().path("token");
    }

    @Test
    public void naoDeveAcessarSemToken() {
        given()
                .when()
                    .get("/contas")
                .then()
                    .statusCode(401);
    }

    @Test
    public void deveIncluirContaComSucesso(){
                given()
                        .header("Authorization", "JWT " + token)
                        .body("{\"nome\": \"Conta para teste restAssured\"}")
                .when()
                    .post("/contas")
                .then()
                        .statusCode(201)
                ;
    }

    @Test
    public void alterarContaComSucesso() {
        given()
                    .header("Authorization", "JWT " + token)
                    .body("{\"nome\": \"Conta alterada\"}")
                .when()
                    .put("/contas/2492738")
                .then()
                    .statusCode(200)
                .log().all()
                .body("nome", is("Conta alterada"))
        ;
    }

    @Test
    public void naoDeveIncluirContaComMesmoNome(){
        given()
                .header("Authorization", "JWT " + token)
                .body("{\"nome\": \"Conta alterada\"}")
            .when()
                .post("/contas")
            .then()
                .statusCode(400)
                .body("error", is("Já existe uma conta com esse nome!"))
        ;
    }

    @Test
    public void deveInserirMovimentacaoComSucesso(){
        Movimentacao movimentacao = getMovimentacaoValida();

        given()
                .header("Authorization", "JWT " + token)
                .body(movimentacao)
            .when()
                .post("/transacoes")
            .then()
                .statusCode(201)
                ;
    }

    @Test
    public void validarCamposObrigatoriosNaMovimentacao() {
        given()
                .header("Authorization", "JWT " + token)
                .body("{}")
            .when()
                .post("/transacoes")
            .then()
                .statusCode(400)
                .body("$", hasSize(8))
                .body("msg", hasItems(
                        "Data da Movimentação é obrigatório",
                        "Data do pagamento é obrigatório",
                        "Descrição é obrigatório",
                        "Interessado é obrigatório",
                        "Valor é obrigatório",
                        "Valor deve ser um número",
                        "Conta é obrigatório",
                        "Situação é obrigatório"
                ))
                ;
    }

    private Movimentacao getMovimentacaoValida() {
        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setConta_id(2492738);
        movimentacao.setDescricao("Teste descrição da movimentação");
        movimentacao.setEnvolvido("Envolvido na mov");
        movimentacao.setTipo("REC");
        movimentacao.setData_transacao("01/01/2025");
        movimentacao.setData_pagamento("10/06/2025");
        movimentacao.setStatus(true);

        return movimentacao;
    }

}
