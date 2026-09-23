/*
alunos:
Abílo Batista
Beatriz Cunha
Matheus Pelissari
Samuel Pereira
 */

import java.util.ArrayDeque;
import java.util.Arrays;
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

    // caminho de maior dependência acumulada entre origem e destino, de forma aproximada:
    // adapta o Dijkstra usando 1/peso como custo da aresta, já que peso maior representa
    // maior dependência. minimizar a soma de 1/peso tende a preferir arestas de peso alto,
    // aproximando (sem garantir) o caminho de maior soma de pesos originais.
    public CaminhoCritico caminhoCritico(int origem, int destino) {
        if (!verticeValido(origem) || !verticeValido(destino)) {
            return new CaminhoCritico(Collections.emptyList(), 0);
        }

        double[] custo = new double[numVertices];
        int[] anterior = new int[numVertices];
        boolean[] visitado = new boolean[numVertices];
        java.util.Arrays.fill(custo, Double.POSITIVE_INFINITY);
        java.util.Arrays.fill(anterior, -1);
        custo[origem] = 0;

        for (int cont = 0; cont < numVertices; cont++) {
            int u = -1;
            double menor = Double.POSITIVE_INFINITY;
            for (int i = 0; i < numVertices; i++) {
                if (!visitado[i] && custo[i] < menor) {
                    menor = custo[i];
                    u = i;
                }
            }
            if (u == -1) break;

            // finalizar u e nunca reabri-lo garante que o caminho reconstruído por
            // "anterior" não forme ciclo, mesmo que o grafo original tenha um.
            visitado[u] = true;
            if (u == destino) break;

            for (Aresta a : vertices[u].adjacencias) {
                if (visitado[a.destino]) continue;
                double custoAresta = 1.0 / a.peso;
                if (custo[u] + custoAresta < custo[a.destino]) {
                    custo[a.destino] = custo[u] + custoAresta;
                    anterior[a.destino] = u;
                }
            }
        }

        if (custo[destino] == Double.POSITIVE_INFINITY) {
            return new CaminhoCritico(Collections.emptyList(), 0);
        }

        List<Integer> caminho = new ArrayList<>();
        int dependencia = 0;
        for (int v = destino; v != -1; v = anterior[v]) {
            caminho.add(v);
            if (anterior[v] != -1) {
                dependencia += pesoAresta(anterior[v], v);
            }
        }
        Collections.reverse(caminho);
        return new CaminhoCritico(caminho, dependencia);
    }

    private int pesoAresta(int i, int j) {
        for (Aresta a : vertices[i].adjacencias) {
            if (a.destino == j) return a.peso;
        }
        return 0;
    }

    public void imprime_caminho_critico(int origem, int destino) {
        CaminhoCritico r = caminhoCritico(origem, destino);
        if (r.caminho.isEmpty()) {
            System.out.println("Não há caminho entre " + getInformacao(origem)
                    + " e " + getInformacao(destino) + ".");
            return;
        }
        System.out.print("Caminho crítico de " + getInformacao(origem) + " até "
                + getInformacao(destino) + ": ");
        for (int i = 0; i < r.caminho.size(); i++) {
            System.out.print(getInformacao(r.caminho.get(i)));
            if (i < r.caminho.size() - 1) System.out.print(" -> ");
        }
        System.out.println();
        System.out.println("Dependência acumulada: " + r.dependenciaAcumulada);
    }

    public int getNumArestas() {
        int totalArestas = 0;
        for (int i = 0; i < numVertices; i++) {
            totalArestas += vertices[i].adjacencias.size();
        }
        return totalArestas;
    }

    // calcula e exibe os 20 indivíduos com maior grau de saída e entrada
    public void imprimeTop20Graus() {
        int[] grauSaida = new int[numVertices];
        int[] grauEntrada = new int[numVertices];

        // calcula os graus de saída e entrada
        for (int i = 0; i < numVertices; i++) {
            int qtdAdjacencias = vertices[i].adjacencias.size();
            grauSaida[i] = qtdAdjacencias;

            for (int j = 0; j < qtdAdjacencias; j++) {
                Aresta a = vertices[i].adjacencias.get(j);
                grauEntrada[a.destino]++;
            }
        }

        System.out.println("\n--- Top 20 Indivíduos com Maior Grau de Saída ---");
        imprimeTop20(grauSaida);

        System.out.println("\n--- Top 20 Indivíduos com Maior Grau de Entrada ---");
        imprimeTop20(grauEntrada);
    }

    // ordena e imprime os 20 maiores valores de um vetor de graus
    private void imprimeTop20(int[] graus) {
        int[] maioresGraus = new int[20];
        int[] indicesMaiores = new int[20];

        for (int i = 0; i < 20; i++) {
            indicesMaiores[i] = -1;
            maioresGraus[i] = -1;
        }

        for (int i = 0; i < numVertices; i++) {
            int grauAtual = graus[i];

            if (grauAtual > maioresGraus[19]) {
                int posicaoInsercao = 19;

                // empurra os menores para baixo para abrir espaço na posição correta
                while (posicaoInsercao > 0 && grauAtual > maioresGraus[posicaoInsercao - 1]) {
                    maioresGraus[posicaoInsercao] = maioresGraus[posicaoInsercao - 1];
                    indicesMaiores[posicaoInsercao] = indicesMaiores[posicaoInsercao - 1];
                    posicaoInsercao--;
                }

                // insere o novo grau e índice na posição aberta
                maioresGraus[posicaoInsercao] = grauAtual;
                indicesMaiores[posicaoInsercao] = i;
            }
        }

        for (int i = 0; i < 20; i++) {
            int v = indicesMaiores[i];
            if (v == -1 || maioresGraus[i] == 0) break;

            System.out.println((i + 1) + "º: " + getInformacao(v) + " (Grau: " + maioresGraus[i] + ")");
        }
    }

    // inicia a busca em profundidade retornando a lista de nós do caminho
    public List<Integer> buscaEmProfundidade(int origem, int destino) {
        if (!verticeValido(origem) || !verticeValido(destino)) {
            return Collections.emptyList();
        }

        boolean[] visitado = new boolean[numVertices];
        List<Integer> caminho = new ArrayList<>();

        if (dfsRecursiva(origem, destino, visitado, caminho)) {
            return caminho;
        }

        return Collections.emptyList();
    }

    // recursão da busca, o vetor de visitados é compartilhado para evitar ciclos
    private boolean dfsRecursiva(int u, int destino, boolean[] visitado, List<Integer> caminho) {
        visitado[u] = true;
        caminho.add(u);

        if (u == destino) {
            return true;
        }

        int qtdAdjacencias = vertices[u].adjacencias.size();

        for (int i = 0; i < qtdAdjacencias; i++) {
            Aresta a = vertices[u].adjacencias.get(i);

            if (!visitado[a.destino]) {
                if (dfsRecursiva(a.destino, destino, visitado, caminho)) {
                    return true;
                }
            }
        }

        caminho.remove(caminho.size() - 1);
        return false;
    }

    public void imprimeDFS(int origem, int destino) {
        List<Integer> caminho = buscaEmProfundidade(origem, destino);

        if (caminho.isEmpty()) {
            System.out.println("Não há caminho alcançável de " + getInformacao(origem)
                    + " até " + getInformacao(destino) + " (DFS).");
            return;
        }

        int saltos = caminho.size() - 1;
        System.out.println("\nCaminho DFS de " + getInformacao(origem) + " até " + getInformacao(destino) + ":");
        System.out.println("Total de indivíduos no caminho: " + caminho.size());
        System.out.println("Total de saltos: " + saltos);
        System.out.println("Trajeto:");

        for (int i = 0; i < caminho.size(); i++) {
            if (i == 0) {
                System.out.println("  [Origem]  " + getInformacao(caminho.get(i)));
            } else if (i == caminho.size() - 1) {
                System.out.println("  [Destino] " + getInformacao(caminho.get(i)));
            } else {
                System.out.println("    (" + i + ") -> " + getInformacao(caminho.get(i)));
            }
        }
    }

    // busca em largura de origem até destino. devolve o caminho (lista de índices)
    // ou lista vazia se destino não for alcançável. como a BFS explora o grafo em
    // camadas (distância 1, depois 2, ...), o caminho encontrado é o de menor
    // número de saltos. nosVisitados, se não for null, recebe a ordem de visita.
    public List<Integer> buscaEmLargura(int origem, int destino, List<Integer> nosVisitados) {
        if (!verticeValido(origem) || !verticeValido(destino)) {
            return Collections.emptyList();
        }
 
        boolean[] visitado = new boolean[numVertices];
        int[] anterior = new int[numVertices];   // de qual vértice cada um foi descoberto
        Arrays.fill(anterior, -1);
 
        ArrayDeque<Integer> fila = new ArrayDeque<>();
        visitado[origem] = true;   // marca ao entrar na fila: nenhum vértice entra duas vezes
        fila.add(origem);
 
        boolean achou = false;
        while (!fila.isEmpty()) {
            int u = fila.poll();
            if (nosVisitados != null) nosVisitados.add(u);
 
            if (u == destino) {
                achou = true;
                break;
            }
 
            for (Aresta a : vertices[u].adjacencias) {
                // o vetor de visitados impede reentrar em vértices já vistos,
                // então ciclos do grafo não geram loop infinito.
                if (!visitado[a.destino]) {
                    visitado[a.destino] = true;
                    anterior[a.destino] = u;
                    fila.add(a.destino);
                }
            }
        }
 
        if (!achou) {
            return Collections.emptyList();
        }
 
        // reconstrói o caminho voltando do destino até a origem pelo vetor anterior
        List<Integer> caminho = new ArrayList<>();
        for (int v = destino; v != -1; v = anterior[v]) {
            caminho.add(v);
        }
        Collections.reverse(caminho);
        return caminho;
    }
 
    public List<Integer> buscaEmLargura(int origem, int destino) {
        return buscaEmLargura(origem, destino, null);
    }
 
    public void imprimeBFS(int origem, int destino) {
        List<Integer> visitados = new ArrayList<>();
        List<Integer> caminho = buscaEmLargura(origem, destino, visitados);
 
        if (caminho.isEmpty()) {
            System.out.println("Não há caminho alcançável de " + getInformacao(origem)
                    + " até " + getInformacao(destino) + " (BFS).");
            System.out.println("Vértices visitados na busca: " + visitados.size());
            return;
        }
 
        System.out.println("\nCaminho BFS de " + getInformacao(origem) + " até " + getInformacao(destino) + ":");
        System.out.println("Vértices visitados na busca: " + visitados.size());
        System.out.println("Total de indivíduos no caminho: " + caminho.size());
        System.out.println("Total de saltos (menor possível): " + (caminho.size() - 1));
        System.out.println("Trajeto:");
 
        for (int i = 0; i < caminho.size(); i++) {
            if (i == 0) {
                System.out.println("  [Origem]  " + getInformacao(caminho.get(i)));
            } else if (i == caminho.size() - 1) {
                System.out.println("  [Destino] " + getInformacao(caminho.get(i)));
            } else {
                System.out.println("    (" + i + ") -> " + getInformacao(caminho.get(i)));
            }
        }
    }
 
    // nós que estão exatamente a D arestas de N. a distância usada é a menor
    // possível (em número de arestas), calculada por uma BFS em camadas a partir de N.
    // um nó alcançável em 1 e em 3 saltos está a distância 1, não 3.
    public List<Integer> nosADistancia(int n, int d) {
        List<Integer> resultado = new ArrayList<>();
        if (!verticeValido(n) || d < 0) {
            return resultado;
        }
 
        int[] distancia = new int[numVertices];
        Arrays.fill(distancia, -1);   // -1 = ainda não visitado; evita ciclos
        distancia[n] = 0;
 
        ArrayDeque<Integer> fila = new ArrayDeque<>();
        fila.add(n);
 
        while (!fila.isEmpty()) {
            int u = fila.poll();
 
            if (distancia[u] == d) {
                resultado.add(u);
                continue;   // não precisa expandir além da distância pedida
            }
 
            for (Aresta a : vertices[u].adjacencias) {
                if (distancia[a.destino] == -1) {
                    distancia[a.destino] = distancia[u] + 1;
                    fila.add(a.destino);
                }
            }
        }
 
        return resultado;
    }
 
    public void imprimeNosADistancia(int n, int d) {
        if (d < 0) {
            System.out.println("A distância deve ser maior ou igual a zero.");
            return;
        }
        List<Integer> nos = nosADistancia(n, d);
        System.out.println("\nNós a distância " + d + " de " + getInformacao(n) + ": " + nos.size());
        for (int v : nos) {
            System.out.println("  - " + getInformacao(v));
        }
    }
}