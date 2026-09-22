import java.util.List;

// resultado de GrafoEnron.caminhoCritico: sequência de vértices e a dependência acumulada
// (soma dos pesos originais das arestas) ao longo do caminho.
public class CaminhoCritico {
    public final List<Integer> caminho;
    public final int dependenciaAcumulada;

    CaminhoCritico(List<Integer> caminho, int dependenciaAcumulada) {
        this.caminho = caminho;
        this.dependenciaAcumulada = dependenciaAcumulada;
    }
}
