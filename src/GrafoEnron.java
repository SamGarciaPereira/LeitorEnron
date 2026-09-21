/*
alunos:
Abílo Batista
Beatriz Cunha
Matheus Pelissari
Samuel Pereira
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// grafo direcionado, ponderado e rotulado. não depende do domínio de e-mail.
public class GrafoEnron {

    public static class Aresta {
        private final int destino;   // índice do vértice de destino
        private int peso;

        Aresta(int destino, int peso) {
            this.destino = destino;
            this.peso = peso;
        }

        public int getDestino() {
            return destino;
        }

        public int getPeso() {
            return peso;
        }
    }

    private static class Vertice {
        String informacao = "";
        List<Aresta> adjacencias = new ArrayList<>();
    }

    private static final int INFINITO = Integer.MAX_VALUE;

    private final Vertice[] vertices;
    private final int numVertices;

    // rótulo -> índice. evita varrer todos os vértices a cada consulta.
    private final Map<String, Integer> indicePorInformacao = new HashMap<>();

    public GrafoEnron(int numVertices) {
        this.numVertices = numVertices;
        this.vertices = new Vertice[numVertices];
        for (int i = 0; i < numVertices; i++) {
            vertices[i] = new Vertice();
        }
    }

    public void cria_adjacencia(int i, int j, int P) {
        if (!verticeValido(i) || !verticeValido(j)) {
            System.out.println("Erro: vértice inválido.");
            return;
        }
        // aresta já existente tem o peso substituído, não acumulado.
        for (Aresta a : vertices[i].adjacencias) {
            if (a.destino == j) {
                a.peso = P;
                return;
            }
        }
        vertices[i].adjacencias.add(new Aresta(j, P));
    }

    public void remove_adjacencia(int i, int j) {
        if (!verticeValido(i) || !verticeValido(j)) {
            System.out.println("Erro: vértice inválido.");
            return;
        }
        // a remoção exige iterator: retirar elementos de uma lista percorrida
        // por for-each lança ConcurrentModificationException.
        Iterator<Aresta> it = vertices[i].adjacencias.iterator();
        while (it.hasNext()) {
            if (it.next().destino == j) {
                it.remove();
                return;
            }
        }
    }

    public void imprime_adjacencias() {
        for (int i = 0; i < numVertices; i++) {
            System.out.print(i + " (" + vertices[i].informacao + "):");
            for (Aresta a : vertices[i].adjacencias) {
                System.out.print(" -> " + a.destino + "[" + a.peso + "]");
            }
            System.out.println();
        }
    }

    public void seta_informacao(int i, String V) {
        if (!verticeValido(i)) {
            System.out.println("Erro: vértice inválido.");
            return;
        }
        // o rótulo anterior deixa de apontar para este vértice.
        indicePorInformacao.remove(vertices[i].informacao, i);
        vertices[i].informacao = V;
        indicePorInformacao.put(V, i);
    }

    public String getInformacao(int i) {
        return verticeValido(i) ? vertices[i].informacao : null;
    }

    public int adjacentes(int i, int[] adj) {
        if (!verticeValido(i)) {
            return 0;
        }
        int qtd = vertices[i].adjacencias.size();
        for (int k = 0; k < qtd; k++) {
            adj[k] = vertices[i].adjacencias.get(k).destino;
        }
        return qtd;
    }

    public int getNumVertices() {
        return numVertices;
    }

    // índice do vértice com esse rótulo, ou -1 se não existir.
    public int indiceDe(String informacao) {
        return indicePorInformacao.getOrDefault(informacao, -1);
    }

    //arestas de saída do vértice i. a lista é somente leitura.
    public List<Aresta> arestas(int i) {
        if (!verticeValido(i)) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(vertices[i].adjacencias);
    }

    private boolean verticeValido(int i) {
        return i >= 0 && i < numVertices;
    }

    //distância de custo mínimo da origem até cada vértice.
    public int[] dijkstra(int origem) {
        int[] dist = new int[numVertices];
        boolean[] visitado = new boolean[numVertices];
        java.util.Arrays.fill(dist, INFINITO);

        if (!verticeValido(origem)) {
            return dist;
        }
        dist[origem] = 0;

        for (int cont = 0; cont < numVertices; cont++) {
            // seleciona o vértice não visitado de menor distância acumulada.
            int u = -1;
            int menor = INFINITO;
            for (int i = 0; i < numVertices; i++) {
                if (!visitado[i] && dist[i] < menor) {
                    menor = dist[i];
                    u = i;
                }
            }
            if (u == -1) break;   // o restante do grafo é inalcançável

            // marcar como visitado impede reprocessamento e trata os ciclos.
            visitado[u] = true;

            for (Aresta a : vertices[u].adjacencias) {
                if (!visitado[a.destino] && dist[u] + a.peso < dist[a.destino]) {
                    dist[a.destino] = dist[u] + a.peso;
                }
            }
        }

        return dist;
    }

    public void imprime_dijkstra(int origem) {
        int[] dist = dijkstra(origem);
        System.out.println("Distâncias mínimas a partir do vértice " + origem
                + " (" + getInformacao(origem) + "):");
        for (int i = 0; i < numVertices; i++) {
            String rotulo = getInformacao(i);
            if (dist[i] == INFINITO) {
                System.out.println("  -> " + i + " (" + rotulo + "): inalcançável");
            } else {
                System.out.println("  -> " + i + " (" + rotulo + "): " + dist[i]);
            }
        }
    }
}