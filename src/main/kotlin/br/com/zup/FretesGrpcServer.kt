package br.com.zup

import com.google.protobuf.Any
import com.google.rpc.Code
import com.google.rpc.StatusProto
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.lang.IllegalStateException
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FretesGrpcServer: FretesServiceGrpc.FretesServiceImplBase() {

    private val logger = LoggerFactory.getLogger(FretesGrpcServer::class.java)

    override fun calculaFrete(request: CalculaFreteRequest?, responseObserver: StreamObserver<CalculaFreteResponse>?) {

        logger.info("Calculando frete para o request: ${request}")

        val cep = request?.cep
        if(cep == null || cep.isBlank()){
            val error =  Status.INVALID_ARGUMENT
                            .withDescription("Cpf deve ser informado")
                            .asRuntimeException()
            responseObserver?.onError(error)
        }

        if(!cep!!.matches("[0-9]{5}-[0-9]{3}".toRegex())){
            val error = Status.INVALID_ARGUMENT
                .withDescription("Cpf no formato Invalido")
                .augmentDescription("O formato esperado deve ser 99999-999")
                .asRuntimeException()
            responseObserver?.onError(error)
        }

        //SIMULAR ERRO DE SEGURANCA

        if(cep.endsWith("333")){

            val statusProto = com.google.rpc.Status.newBuilder()
                                .setCode(Code.PERMISSION_DENIED.number)
                                .setMessage("Usuario nao pode acessar este recurso")
                                .addDetails(Any.pack(ErrorDetails.newBuilder()
                                    .setCode(401)
                                    .setMessage("Token Invalido")
                                    .build()))
                                .build()
            // DETALHES DO ADDDETAILS VAI NOS METADADOS
            // ( NO BLOOM RPC AINDA N E POSSIVEL VISUALIZAR OS METADADOS RECEBIDOS)
            val error = io.grpc.protobuf.StatusProto.toStatusRuntimeException(statusProto)
            responseObserver?.onError(error)
        }

        var valor = 0.0
        try {
            valor = Random.nextDouble(from = 15.00, until = 200.00)
            if(valor > 100){
                throw IllegalStateException("Erro inesperado no sistema")
            }
        }catch (e: Exception){
            responseObserver?.onError(Status.INTERNAL
                .withDescription(e.message)
                .withCause(e)
                .asRuntimeException()
            )
        }

        val response = CalculaFreteResponse.newBuilder()
            .setCep(request!!.cep)
            .setFrete(valor)
            .build()
        logger.info("Frete calculado: ${response}")
        responseObserver!!.onNext(response)
        responseObserver.onCompleted()

    }
}