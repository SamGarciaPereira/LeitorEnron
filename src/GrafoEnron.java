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
        private int peso;   // nº de mensagens 

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
        String informacao = "";   // o rótulo (e-mail)
        List<Aresta> adjacencias = new ArrayList<>();
    }

    private static final int INFINITO = Integer.MAX_VALUE;

    private final Vertice[] vertices;
    private final int numVertices;

    // rótulo (e-mail) -> índice. evita varrer todos os vértices a cada consulta.
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

    // =====================================================================
    // QUESTÃO 1 - verificação de ciclo
    // =====================================================================

    // estados da DFS: BRANCO = não visitado, CINZA = na pilha de recursão
    // (caminho atual), PRETO = vértice e todos os seus descendentes concluídos.
    private static final int BRANCO = 0;
    private static final int CINZA = 1;
    private static final int PRETO = 2;

    // o grafo é cíclico se contém ao menos um ciclo dirigido.
    public boolean ehCiclico() {
        return !encontraCiclo().isEmpty();
    }

    // devolve um ciclo dirigido (v0 -> v1 -> ... -> v0) ou lista vazia se o grafo
    // for acíclico. a DFS percorre todos os vértices: num grafo dirigido há ciclo
    // se, e somente se, a busca encontra uma aresta de retorno, isto é, uma aresta
    // u -> v em que v ainda está CINZA (é ancestral de u no caminho atual).
    // aresta para vértice PRETO não indica ciclo: v já foi concluído por outro ramo.
    // a DFS é iterativa (pilha explícita) para não estourar a pilha da JVM nos
    // caminhos longos do grafo da Enron.
    public List<Integer> encontraCiclo() {
        int[] cor = new int[numVertices];      // inicia tudo BRANCO
        int[] pai = new int[numVertices];      // para reconstruir o ciclo
        int[] proxAresta = new int[numVertices]; // próxima aresta a examinar de cada vértice
        ArrayDeque<Integer> pilha = new ArrayDeque<>();

        for (int s = 0; s < numVertices; s++) {
            if (cor[s] != BRANCO) continue;
            cor[s] = CINZA;
            pai[s] = -1;
            pilha.push(s);

            while (!pilha.isEmpty()) {
                int u = pilha.peek();
                List<Aresta> adj = vertices[u].adjacencias;

                if (proxAresta[u] < adj.size()) {
                    int v = adj.get(proxAresta[u]++).destino;
                    if (cor[v] == BRANCO) {
                        cor[v] = CINZA;
                        pai[v] = u;
                        pilha.push(v);
                    } else if (cor[v] == CINZA) {
                        // aresta de retorno u -> v: o ciclo é v -> ... -> u -> v
                        List<Integer> ciclo = new ArrayList<>();
                        for (int w = u; w != v; w = pai[w]) {
                            ciclo.add(w);
                        }
                        ciclo.add(v);
                        Collections.reverse(ciclo);
                        ciclo.add(v);   // fecha o ciclo
                        return ciclo;
                    }
                } else {
                    cor[u] = PRETO;   // todas as arestas de u examinadas
                    pilha.pop();
                }
            }
        }
        return Collections.emptyList();
    }

    public void imprimeCiclo() {
        List<Integer> ciclo = encontraCiclo();
        if (ciclo.isEmpty()) {
            System.out.println("O grafo é ACÍCLICO (não há ciclo dirigido).");
            return;
        }
        System.out.println("O grafo é CÍCLICO. Exemplo de ciclo ("
                + (ciclo.size() - 1) + " aresta(s)):");
        for (int i = 0; i < ciclo.size(); i++) {
            System.out.print(getInformacao(ciclo.get(i)));
            if (i < ciclo.size() - 1) System.out.print(" -> ");
        }
        System.out.println();
    }

    // =====================================================================
    // QUESTÃO 2 - componentes (fracamente conexos)
    // =====================================================================

    // identifica os componentes do grafo ignorando o sentido das arestas: u e v
    // ficam no mesmo componente se houver caminho entre eles quando cada aresta
    // u -> v é tratada como u - v. cada componente é a lista ordenada de índices
    // dos vértices membros; a lista de componentes vem do maior para o menor.
    // complexidade O(V + E): cada vértice entra uma vez na fila e cada aresta é
    // examinada uma vez em cada sentido.
    public List<List<Integer>> componentes() {
        // a lista de adjacência guarda apenas as arestas de saída. para andar no
        // sentido contrário, monta-se uma vez a lista de predecessores (entrada).
        List<List<Integer>> entrada = new ArrayList<>(numVertices);
        for (int i = 0; i < numVertices; i++) {
            entrada.add(new ArrayList<>());
        }
        for (int u = 0; u < numVertices; u++) {
            for (Aresta a : vertices[u].adjacencias) {
                entrada.get(a.destino).add(u);
            }
        }

        boolean[] visitado = new boolean[numVertices];
        List<List<Integer>> componentes = new ArrayList<>();
        ArrayDeque<Integer> fila = new ArrayDeque<>();

        for (int s = 0; s < numVertices; s++) {
            if (visitado[s]) continue;

            // s não pertence a nenhum componente já encontrado: inicia um novo
            // e uma BFS recolhe todos os vértices alcançáveis sem considerar sentido.
            List<Integer> membros = new ArrayList<>();
            visitado[s] = true;
            fila.add(s);

            while (!fila.isEmpty()) {
                int u = fila.poll();
                membros.add(u);

                for (Aresta a : vertices[u].adjacencias) {   // u -> v
                    if (!visitado[a.destino]) {
                        visitado[a.destino] = true;
                        fila.add(a.destino);
                    }
                }
                for (int v : entrada.get(u)) {                // v -> u
                    if (!visitado[v]) {
                        visitado[v] = true;
                        fila.add(v);
                    }
                }
            }

            Collections.sort(membros);
            componentes.add(membros);
        }

        componentes.sort((a, b) -> Integer.compare(b.size(), a.size()));
        return componentes;
    }

    // resumo: quantidade, distribuição de tamanhos e os membros dos componentes.
    // os componentes com mais de maxMembros vértices são exibidos parcialmente.
    public void imprimeComponentes(int maxComponentes, int maxMembros) {
        List<List<Integer>> comps = componentes();
        System.out.println("\nNúmero de componentes: " + comps.size());
        if (comps.isEmpty()) return;

        System.out.println("Maior componente: " + comps.get(0).size() + " vértices ("
                + String.format("%.2f", 100.0 * comps.get(0).size() / numVertices) + "% do grafo)");

        // tamanho -> quantidade de componentes com esse tamanho
        java.util.TreeMap<Integer, Integer> distribuicao =
                new java.util.TreeMap<>(Collections.reverseOrder());
        for (List<Integer> c : comps) {
            distribuicao.merge(c.size(), 1, Integer::sum);
        }
        System.out.println("Distribuição (tamanho: quantidade de componentes):");
        for (Map.Entry<Integer, Integer> e : distribuicao.entrySet()) {
            System.out.println("  " + e.getKey() + " vértice(s): " + e.getValue());
        }

        int limite = Math.min(maxComponentes, comps.size());
        System.out.println("\nPrimeiros " + limite + " componentes:");
        for (int i = 0; i < limite; i++) {
            List<Integer> c = comps.get(i);
            System.out.print("C" + (i + 1) + " (" + c.size() + "): ");
            for (int k = 0; k < Math.min(maxMembros, c.size()); k++) {
                if (k > 0) System.out.print(", ");
                System.out.print(getInformacao(c.get(k)));
            }
            if (c.size() > maxMembros) System.out.print(", ... (+" + (c.size() - maxMembros) + ")");
            System.out.println();
        }
    }

    // grava todos os componentes, com todos os membros, num arquivo texto.
    public void salvaComponentes(String arquivo) throws java.io.IOException {
        List<List<Integer>> comps = componentes();
        try (java.io.PrintWriter out = new java.io.PrintWriter(
                java.nio.file.Files.newBufferedWriter(java.nio.file.Paths.get(arquivo),
                        java.nio.charset.StandardCharsets.UTF_8))) {
            out.println("Total de componentes: " + comps.size());
            for (int i = 0; i < comps.size(); i++) {
                List<Integer> c = comps.get(i);
                out.println();
                out.println("Componente " + (i + 1) + " - " + c.size() + " vértice(s)");
                for (int v : c) {
                    out.println("  " + getInformacao(v));
                }
            }
        }
    }

    // =====================================================================
    // QUESTÃO 3 - verificação de conexidade
    // =====================================================================

    // a partir da questão 2: o grafo é (fracamente) conexo se todos os vértices
    // estão num único componente. por convenção, o grafo vazio é considerado conexo.
    public boolean ehConexo() {
        return componentes().size() <= 1;
    }

    public void imprimeConexidade() {
        int qtd = componentes().size();
        if (qtd <= 1) {
            System.out.println("O grafo é CONEXO (fracamente conexo): 1 componente.");
        } else {
            System.out.println("O grafo é DESCONEXO: " + qtd + " componentes.");
        }
    }
}
