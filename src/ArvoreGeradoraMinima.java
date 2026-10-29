/*
Nomes:
Abílio Batista
Beatriz Cunha
Samuel Pereira
 */


import java.util.List;

public class ArvoreGeradoraMinima {
    public final List<int[]> arestas;
    public final long pesoTotal;
    public final int componentes;

    ArvoreGeradoraMinima(List<int[]> arestas, long pesoTotal, int componentes) {
        this.arestas = arestas;
        this.pesoTotal = pesoTotal;
        this.componentes = componentes;
    }
}
