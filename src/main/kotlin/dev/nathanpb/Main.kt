package dev.nathanpb

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document

class Option(val key: Char, val description: String, val executor: () -> Unit)

const val bar = "========================================\n"
val mongoClient : MongoClient               = MongoClients.create("mongodb://admin:admin@<DB_HOST>/?authSource=admin")
val db          : MongoDatabase             = mongoClient.getDatabase("veiculosdb")
val collection  : MongoCollection<Document> = db.getCollection("veiculos")

val options = arrayOf(
    Option('c', "Criar Veículo") {
        try {
            Document()
                .append("marca",  input("Marca: " ))
                .append("modelo", input("Modelo: "))
                .append("placa",  input("Placa: " ))
                .append("cor",    input("Cor: "   ))
                .append("ano",    input("Ano: "   ).toInt())
                .let {
                    collection.insertOne(it)
                    println("Veículo adicionado com sucesso!")
                    println("Documento do Veículo: ${collection.find(Document().append("placa", it.getString("placa"))).first()}")
                }
        } catch (ex: java.lang.Exception) {
            println("Houve uma falha ao adicionar o veículo!")
        }
    },
    Option('v', "Ver Veículos"){
        collection.find().joinToString("\n$bar") {
            it.entries.joinToString("\n") { entry ->
                "${entry.key.capitalize()}: ${entry.value}"
            }
        }.let { println("$bar$it") }
    },
    Option('r', "Remover Veículo"){
        collection.findOneAndDelete(
            Document().append("placa", input("Placa do Veículo: "))
        ).let {
            println(if(it == null){
                "Veículo não encontrado!"
            } else {
                "Veículo removido com sucesso!"
            })
        }
    },
    Option('s', "Sair") { System.exit(0) }
)

fun input(msg: String) = print(msg).run { readLine().orEmpty() }

fun runOnce(options: Array<Option>) {
    println("$bar\nVeículos Cadastrados: ${collection.countDocuments()}")
    options.joinToString(separator = "\n") {
        "${it.key.toUpperCase()} - ${it.description}"
    }.let { println(it) }

    (
        input("Insira uma opção: ")[0].let { opcao ->
            options.firstOrNull { it.key.toUpperCase() == opcao.toUpperCase() }
        } ?: throw Exception("Opção Inválida!")
    ).executor()
}

fun main() {
    while (true) {
        try {
            runOnce(options)
        } catch (ex: Exception) {
            println(ex.message)
        }
    }
}