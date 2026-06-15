# Knowledge Graph

## Overview

The Knowledge Graph discovers and stores relationships between entities in the user's knowledge base. It enables graph-based querying, relationship visualization, and intelligent context discovery.

## Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                    Knowledge Graph                            │
│                                                              │
│  ┌─────────────────┐  ┌────────────────┐  ┌───────────────┐  │
│  │ Entity           │  │ Relationship   │  │ Graph         │  │
│  │ Extraction       │  │ Discovery      │  │ Traversal     │  │
│  │                  │  │                │  │               │  │
│  │ LLM-based NER   │  │ Co-occurrence  │  │ BFS/DFS       │  │
│  │ Pattern-based   │  │ Sequential     │  │ Path finding  │  │
│  │ Custom entities │  │ Explicit links │  │ Connected     │  │
│  │                  │  │                │  │  components   │  │
│  └────────┬────────┘  └───────┬────────┘  └───────┬───────┘  │
│           │                  │                    │           │
│           └─────────┬────────┴────────────────────┘           │
│                     │                                         │
│            ┌────────▼────────┐                                │
│            │  Graph Database │                                │
│            │  (PostgreSQL    │                                │
│            │   + knowledge_  │                                │
│            │   nodes/edges)  │                                │
│            └─────────────────┘                                │
└──────────────────────────────────────────────────────────────┘
```

## Entity Extraction

```java
@Service
public class EntityExtractor {

    private final OllamaClient ollamaClient;
    private final KnowledgeNodeRepository nodeRepository;

    /**
     * Extract entities from a container using LLM-based NER.
     */
    public List<KnowledgeNode> extractEntities(Container container) {
        String content = buildExtractionContent(container);
        
        String prompt = """
            Extract key entities from the following content.
            Return as JSON array: [{"label": "...", "type": "..."}]
            
            Entity types: PERSON, CONCEPT, TECHNOLOGY, BOOK_TITLE, MOVIE_TITLE,
                         LANGUAGE, FRAMEWORK, TOPIC, ORGANIZATION, LOCATION
            
            Content: %s
            """.formatted(content);

        String response = ollamaClient.generate(prompt, 0.1);
        List<ExtractedEntity> extracted = parseEntities(response);

        List<KnowledgeNode> nodes = new ArrayList<>();
        for (ExtractedEntity entity : extracted) {
            // Deduplicate - check if node already exists
            Optional<KnowledgeNode> existing = nodeRepository
                .findByOwnerIdAndLabel(container.getOwnerId(), entity.label());
            
            if (existing.isPresent()) {
                nodes.add(existing.get());
            } else {
                KnowledgeNode node = new KnowledgeNode();
                node.setOwnerId(container.getOwnerId());
                node.setLabel(entity.label());
                node.setType(entity.type());
                node.setSourceContainerId(container.getId());
                node.setMetadata(Map.of("extractedFrom", container.getTitle()));
                nodes.add(nodeRepository.save(node));
            }
        }

        return nodes;
    }

    /**
     * Extract technology stack from project containers.
     */
    public List<KnowledgeNode> extractTechStack(SoftwareProjectContainer project) {
        String techStack = (String) project.getMetadata().get("techStack");
        if (techStack == null || techStack.isBlank()) return List.of();

        return Arrays.stream(techStack.split(","))
            .map(String::trim)
            .map(tech -> {
                KnowledgeNode node = new KnowledgeNode();
                node.setOwnerId(project.getOwnerId());
                node.setLabel(tech);
                node.setType("TECHNOLOGY");
                node.setSourceContainerId(project.getId());
                return nodeRepository.save(node);
            })
            .collect(Collectors.toList());
    }

    private String buildExtractionContent(Container container) {
        StringBuilder sb = new StringBuilder();
        sb.append("Title: ").append(container.getTitle()).append("\n");
        sb.append("Description: ").append(container.getDescription()).append("\n");
        sb.append("Type: ").append(container.getType()).append("\n");
        
        // Add metadata content
        if (container.getMetadata() != null) {
            sb.append("Metadata: ").append(container.getMetadata().toString()).append("\n");
        }

        // Add tags
        if (container.getTags() != null) {
            sb.append("Tags: ").append(
                container.getTags().stream().map(Tag::getName).collect(Collectors.joining(", "))
            ).append("\n");
        }

        return sb.toString();
    }

    private List<ExtractedEntity> parseEntities(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(
                extractJson(response),
                new TypeReference<List<ExtractedEntity>>() {}
            );
        } catch (Exception e) {
            log.warn("Failed to parse entity extraction result: {}", response, e);
            return List.of();
        }
    }

    private String extractJson(String response) {
        int start = response.indexOf('[');
        int end = response.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        return "[]";
    }

    private record ExtractedEntity(String label, String type) {}
}
```

## Relationship Discovery

```java
@Service
public class RelationshipDiscoverer {

    private final KnowledgeNodeRepository nodeRepository;
    private final KnowledgeEdgeRepository edgeRepository;
    private final ContainerRepository containerRepository;

    /**
     * Discover relationships between a container and existing knowledge nodes.
     */
    public List<KnowledgeEdge> discoverRelationships(Container container) {
        List<KnowledgeNode> containerNodes = extractEntities(container);
        List<KnowledgeEdge> discovered = new ArrayList<>();

        // 1. Co-occurrence: entities mentioned together
        discovered.addAll(findCoOccurrences(container, containerNodes));

        // 2. Same-type connections
        discovered.addAll(findSameTypeConnections(container));

        // 3. Sequential: container follows previous container of similar type
        discovered.addAll(findSequentialRelationships(container));

        // 4. Explicit links (mentions in notes, references)
        discovered.addAll(findExplicitLinks(container));

        return edgeRepository.saveAll(discovered);
    }

    private List<KnowledgeEdge> findCoOccurrences(Container container, List<KnowledgeNode> nodes) {
        List<KnowledgeEdge> edges = new ArrayList<>();

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                KnowledgeEdge edge = new KnowledgeEdge();
                edge.setSourceNodeId(nodes.get(i).getId());
                edge.setTargetNodeId(nodes.get(j).getId());
                edge.setRelationshipType("CO_OCCURS_WITH");
                edge.setWeight(calculateCoOccurrenceWeight(nodes.get(i), nodes.get(j)));
                edges.add(edge);
            }
        }

        return edges;
    }

    private List<KnowledgeEdge> findSameTypeConnections(Container container) {
        List<Container> sameType = containerRepository
            .findByOwnerIdAndTypeAndStatus(
                container.getOwnerId(),
                container.getType(),
                ContainerStatus.ACTIVE
            );

        // Create edges between containers of same type
        return sameType.stream()
            .filter(c -> !c.getId().equals(container.getId()))
            .map(c -> {
                KnowledgeEdge edge = new KnowledgeEdge();
                // Link via their entity nodes
                edge.setRelationshipType("SAME_CATEGORY");
                edge.setWeight(0.5);
                return edge;
            })
            .collect(Collectors.toList());
    }

    private double calculateCoOccurrenceWeight(KnowledgeNode a, KnowledgeNode b) {
        // Base weight
        double weight = 0.3;

        // Boost if same type category
        if (a.getType().equals(b.getType())) {
            weight += 0.3;
        }

        // Boost for certain relationship pairs
        if ((a.getType().equals("TECHNOLOGY") && b.getType().equals("FRAMEWORK")) ||
            (a.getType().equals("PERSON") && b.getType().equals("ORGANIZATION"))) {
            weight += 0.2;
        }

        return Math.min(1.0, weight);
    }
}
```

## Graph Traversal

```java
@Service
public class GraphTraversalService {

    private final KnowledgeNodeRepository nodeRepository;
    private final KnowledgeEdgeRepository edgeRepository;

    /**
     * Find shortest path between two containers in the knowledge graph.
     */
    public List<GraphPath> findPath(UUID sourceNodeId, UUID targetNodeId, int maxDepth) {
        // BFS traversal
        Set<UUID> visited = new HashSet<>();
        Queue<List<UUID>> queue = new LinkedList<>();
        queue.add(List.of(sourceNodeId));

        while (!queue.isEmpty()) {
            List<UUID> path = queue.poll();
            UUID current = path.get(path.size() - 1);

            if (current.equals(targetNodeId)) {
                return buildPathResult(path);
            }

            if (path.size() >= maxDepth) continue;

            List<KnowledgeEdge> edges = edgeRepository.findBySourceNodeId(current);
            for (KnowledgeEdge edge : edges) {
                UUID next = edge.getTargetNodeId();
                if (!visited.contains(next)) {
                    visited.add(next);
                    List<UUID> newPath = new ArrayList<>(path);
                    newPath.add(next);
                    queue.add(newPath);
                }
            }
        }

        return List.of(); // No path found
    }

    /**
     * Find all containers connected to a given container.
     */
    public List<RelatedContainer> findRelatedContainers(UUID containerId, int maxDepth) {
        // Get entity nodes for container
        List<KnowledgeNode> containerNodes = nodeRepository
            .findBySourceContainerId(containerId);

        Set<UUID> related = new HashSet<>();

        for (KnowledgeNode node : containerNodes) {
            // Find edges from this node
            List<KnowledgeEdge> edges = edgeRepository.findBySourceNodeId(node.getId());
            for (KnowledgeEdge edge : edges) {
                // Find containers connected to target node
                List<KnowledgeNode> targetContainers = nodeRepository
                    .findBySourceContainerIdNotAndId(containerId, edge.getTargetNodeId());
                
                for (KnowledgeNode targetNode : targetContainers) {
                    if (targetNode.getSourceContainerId() != null) {
                        related.add(targetNode.getSourceContainerId());
                    }
                }
            }
        }

        // Fetch container details
        return containerRepository.findAllById(related).stream()
            .map(c -> new RelatedContainer(c.getId(), c.getTitle(), c.getType().name()))
            .collect(Collectors.toList());
    }

    /**
     * Find connected components in the graph.
     * Useful for knowledge base overview.
     */
    public List<GraphCluster> findClusters(UUID userId) {
        List<KnowledgeNode> allNodes = nodeRepository.findByOwnerId(userId);
        List<KnowledgeEdge> allEdges = edgeRepository.findByOwnerId(userId);

        // Union-Find for connected components
        Map<UUID, UUID> parent = new HashMap<>();
        for (KnowledgeNode node : allNodes) {
            parent.put(node.getId(), node.getId());
        }

        for (KnowledgeEdge edge : allEdges) {
            union(parent, edge.getSourceNodeId(), edge.getTargetNodeId());
        }

        // Group by root
        Map<UUID, List<KnowledgeNode>> clusters = new HashMap<>();
        for (KnowledgeNode node : allNodes) {
            UUID root = find(parent, node.getId());
            clusters.computeIfAbsent(root, k -> new ArrayList<>()).add(node);
        }

        return clusters.entrySet().stream()
            .map(entry -> {
                Set<ContainerType> types = entry.getValue().stream()
                    .map(n -> containerRepository.findById(n.getSourceContainerId()))
                    .filter(Optional::isPresent)
                    .map(c -> c.get().getType())
                    .collect(Collectors.toSet());

                return new GraphCluster(
                    entry.getKey(),
                    entry.getValue().size(),
                    types,
                    entry.getValue().stream()
                        .map(KnowledgeNode::getLabel)
                        .collect(Collectors.toList())
                );
            })
            .sorted(Comparator.comparingInt(GraphCluster::size).reversed())
            .collect(Collectors.toList());
    }

    private UUID find(Map<UUID, UUID> parent, UUID x) {
        if (!parent.get(x).equals(x)) {
            parent.put(x, find(parent, parent.get(x)));
        }
        return parent.get(x);
    }

    private void union(Map<UUID, UUID> parent, UUID a, UUID b) {
        UUID rootA = find(parent, a);
        UUID rootB = find(parent, b);
        if (!rootA.equals(rootB)) {
            parent.put(rootA, rootB);
        }
    }
}
```

## Knowledge Graph Visualization

```typescript
// Frontend: KnowledgeGraph.tsx
interface KnowledgeGraphProps {
  containerId?: string;  // Optional - show graph centered on container
  userId: string;
  width?: number;
  height?: number;
}

// Uses D3.js force-directed graph rendering:
// - Nodes: entities (people, concepts, technologies)
// - Edges: relationships between entities
// - Colors: node types
// - Size: node importance (degree centrality)
// - Click: navigate to related container
// - Zoom: pan and zoom
// - Hover: show node details
// - Search: highlight matching nodes
```

## Knowledge Graph Models

```java
@Entity
@Table(name = "knowledge_nodes")
public class KnowledgeNode {
    @Id UUID id;
    UUID ownerId;
    String label;
    String type;           // PERSON, CONCEPT, TECHNOLOGY, etc.
    UUID sourceContainerId;
    @Column(columnDefinition = "jsonb")
    String metadata;
    @Column(columnDefinition = "vector(768)")
    float[] embedding;
    LocalDateTime createdAt;
}

@Entity
@Table(name = "knowledge_edges")
public class KnowledgeEdge {
    @Id UUID id;
    UUID sourceNodeId;
    UUID targetNodeId;
    String relationshipType;  // CO_OCCURS_WITH, SAME_CATEGORY, REFERENCES, PRECEDES
    double weight;
    @Column(columnDefinition = "jsonb")
    String metadata;
    LocalDateTime createdAt;
}
```

## Graph Query Examples

```sql
-- Find all containers related to a specific topic
SELECT DISTINCT c.id, c.title, c.type
FROM containers c
JOIN knowledge_nodes kn ON kn.source_container_id = c.id
WHERE kn.label ILIKE '%machine learning%'
  AND c.owner_id = :userId;

-- Find the most connected entities in a user's knowledge graph
SELECT kn.label, kn.type, COUNT(ke.id) as connection_count
FROM knowledge_nodes kn
LEFT JOIN knowledge_edges ke ON ke.source_node_id = kn.id
WHERE kn.owner_id = :userId
GROUP BY kn.id, kn.label, kn.type
ORDER BY connection_count DESC
LIMIT 20;

-- Find knowledge gaps (topics user hasn't explored)
SELECT suggested_topic
FROM (
  SELECT UNNEST(ARRAY['deep learning', 'reinforcement learning', 'NLP']) as suggested_topic
) topics
WHERE NOT EXISTS (
  SELECT 1 FROM knowledge_nodes
  WHERE owner_id = :userId
    AND label ILIKE '%' || topics.suggested_topic || '%'
);
```
