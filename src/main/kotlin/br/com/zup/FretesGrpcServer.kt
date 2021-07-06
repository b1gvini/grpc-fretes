package br.com.zup

import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FretesGrpcServer: FretesServiceGrpc.FretesServiceImplBase() {

    private val logger = LoggerFactory.getLogger(FretesGrpcServer::class.java)

    override fun calculaFrete(request: CalculaFreteRequest?, responseObserver: StreamObserver<CalculaFreteResponse>?) {

        logger.info("Calculando frete para o request: ${request}")

        val response = CalculaFreteResponse.newBuilder()
            .setCep(request!!.cep)
            .setFrete(Random.nextDouble(from = 15.00, until = 200.00))
            .build()
        logger.info("Frete calculado: ${response}")
        responseObserver!!.onNext(response)
        responseObserver.onCompleted()

    }
}