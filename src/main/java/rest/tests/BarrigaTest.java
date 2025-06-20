package rest.tests;

import io.restassured.RestAssured;
import io.restassured.specification.FilterableRequestSpecification;
import org.junit.BeforeClass;
import org.junit.runner.manipulation.Filterable;
import rest.core.BaseTest;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import rest.utils.DataUtils;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BarrigaTest extends BaseTest {

    private static String contaName = "Conta " + System.nanoTime();
    private static Integer contaId;
    private static Integer movId;

    @BeforeClass
    public static void login() {
        Map<String, String> login = new HashMap<>();
        login.put("email", "wagner@aquino");
        login.put("senha", "123456");

        String token = given()
                .body(login)
            .when()
                .post("/signin")
            .then()
                .statusCode(200)
                .extract().path("token");

        RestAssured.requestSpecification.header("Authorization", "JWT " + token);
    }

    @Test
    public void t02_deveIncluirContaComSucesso(){
        contaId = given()
                        .body("{\"nome\": \"" + contaName + "\"}")
                .when()
                    .post("/contas")
                .then()
                        .statusCode(201)
                        .extract().path("id")
                ;
    }

    @Test
    public void t03_alterarContaComSucesso() {
        given()
                    .body("{\"nome\": \"" + contaName + " alterada\"}")
                    .pathParam("id", contaId)
                .when()
                    .put("/contas/{id}")
                .then()
                    .statusCode(200)
                .log().all()
                .body("nome", is(contaName + " alterada"))
        ;
    }

    @Test
    public void t04_naoDeveIncluirContaComMesmoNome(){
        given()
                .body("{\"nome\": \"" + contaName + " alterada\"}")
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

        movId = given()
                .body(movimentacao)
            .when()
                .post("/transacoes")
            .then()
                .statusCode(201)
                .extract().path("id")
                ;
    }

    @Test
    public void t06_validarCamposObrigatoriosNaMovimentacao() {
        given()
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
        movimentacao.setData_transacao(DataUtils.getDataDiferencaDias(2));

        given()
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
                .pathParam("id", contaId)
            .when()
                .delete("/contas/{id}")
            .then()
                .statusCode(500)
                .body("constraint", is("transacoes_conta_id_foreign"))
        ;
    }

    @Test
    public void t09_deveCalcularSaldoContas() {
        given()
            .when()
                .get("/saldo")
            .then()
                .statusCode(200)
                .body("find{it.conta_id == " + contaId + "}.saldo", is("0.00"))
        ;
    }

    @Test
    public void t10_removerMovimentacao() {
        given()
                .pathParam("id", movId)
            .when()
                .delete("/transacoes/{id}")
            .then()
                .statusCode(204)
        ;
    }

    @Test
    public void t11_validarErroAoremoverMovimentacaoInexistente() {
        given()
            .when()
                .delete("/transacoes/2492739")
            .then()
                .statusCode(404)
                .body("error", is("Não foi encontrada Transacao com id 2492739"))
        ;
    }

    @Test
    public void t12_naoDeveAcessarSemToken() {
        FilterableRequestSpecification requestSpecification = (FilterableRequestSpecification) RestAssured.requestSpecification;
        requestSpecification.removeHeader("Authorization");

        given()
                .when()
                .get("/contas")
                .then()
                .statusCode(401);
    }

    private Movimentacao getMovimentacaoValida() {
        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setConta_id(contaId);
        movimentacao.setDescricao("Teste descrição da movimentação");
        movimentacao.setEnvolvido("Envolvido na mov");
        movimentacao.setTipo("REC");
        movimentacao.setData_transacao(DataUtils.getDataDiferencaDias(-1));
        movimentacao.setData_pagamento(DataUtils.getDataDiferencaDias(5));
        movimentacao.setStatus(true);

        return movimentacao;
    }
}
