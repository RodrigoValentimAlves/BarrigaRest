package rest.tests;

import core.BaseTest;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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
    public void t01_naoDeveAcessarSemToken() {
        given()
                .when()
                    .get("/contas")
                .then()
                    .statusCode(401);
    }

    @Test
    public void t02_deveIncluirContaComSucesso(){
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
    public void t03_alterarContaComSucesso() {
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
    public void t04_naoDeveIncluirContaComMesmoNome(){
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
    public void t05_deveInserirMovimentacaoComSucesso(){
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
    public void t06_validarCamposObrigatoriosNaMovimentacao() {
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

    @Test
    public void t07_naoDeveInserirMovimentacaoComDataFutura(){
        Movimentacao movimentacao = getMovimentacaoValida();
        movimentacao.setData_transacao("20/09/2030");

        given()
                .header("Authorization", "JWT " + token)
            .body(movimentacao)
                .when()
                .post("/transacoes")
            .then()
                .statusCode(400)
                .body("$", hasSize(1))
                .body("msg", hasItem("Data da Movimentação deve ser menor ou igual à data atual"))
        ;
    }

    @Test
    public void t08_naoDeveRemoverContaComMovimentacao() {
        given()
                .header("Authorization", "JWT " + token)
            .when()
                .delete("/contas/2492738")
            .then()
                .statusCode(500)
                .body("constraint", is("transacoes_conta_id_foreign"))
        ;
    }

    @Test
    public void t09_deveCalcularSaldoContas() {
        given()
                .header("Authorization", "JWT " + token)
            .when()
                .get("/saldo")
            .then()
                .statusCode(200)
                .body("find{it.conta_id == 2342594}.saldo", is("-220.00"))
        ;
    }

    @Test
    public void t010_removerMovimentacao() {
        given()
                .header("Authorization", "JWT " + token)
            .when()
                .delete("/transacoes/2492739")
            .then()
                .statusCode(404)
                .body("error", is("Não foi encontrada Transacao com id 2492739"))
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
