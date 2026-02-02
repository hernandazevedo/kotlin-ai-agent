# Kotlin AI Agent

Um agente de IA minimalista que pode explorar, ler e modificar código, construído em Kotlin seguindo o tutorial do JetBrains.

## Características

- **Exploração de Projetos**: Lista diretórios e arquivos
- **Leitura de Código**: Lê e analisa arquivos
- **Edição de Código**: Modifica arquivos baseado em tarefas
- **Integração OpenAI**: Usa GPT-4 para tomada de decisões inteligentes

## Ferramentas Disponíveis

1. **ListDirectoryTool**: Lista todos os arquivos e diretórios em um caminho
2. **ReadFileTool**: Lê o conteúdo de arquivos
3. **EditFileTool**: Modifica arquivos substituindo seu conteúdo
4. **CreateFileTool**: Cria novos arquivos com conteúdo especificado

## Pré-requisitos

- JDK 17 ou superior
- Gradle
- Chave de API da OpenAI

## Configuração

1. Clone o repositório ou crie o projeto

2. Configure sua chave de API da OpenAI:
```bash
export OPENAI_API_KEY="sua-chave-api-aqui"
```

3. Compile o projeto:
```bash
./gradlew build
```

## Uso

Execute o agente fornecendo o caminho do projeto e a tarefa:

```bash
./gradlew run --args="/path/to/project 'Sua tarefa aqui'"
```

### Exemplos

```bash
# Adicionar uma nova função
./gradlew run --args="/Users/nome/meu-projeto 'Adicione uma função para calcular números de Fibonacci'"

# Refatorar código
./gradlew run --args="/Users/nome/meu-projeto 'Refatore a classe UserService para usar injeção de dependência'"

# Corrigir bugs
./gradlew run --args="/Users/nome/meu-projeto 'Corrija o bug no método de autenticação'"
```

## Arquitetura

### Componentes Principais

- **AIAgent**: Orquestra o loop principal de execução
- **PromptExecutor**: Interface com a API da OpenAI
- **ToolRegistry**: Gerencia as ferramentas disponíveis
- **FileSystemProvider**: Abstração para operações de sistema de arquivos
- **Strategy**: Define quando o agente deve continuar ou parar

### Fluxo de Execução

1. O agente recebe uma tarefa e o caminho do projeto
2. Explora o projeto usando `list_directory`
3. Lê arquivos relevantes com `read_file`
4. Faz modificações necessárias via `edit_file`
5. Retorna um resumo das mudanças realizadas

## Limitações

- Máximo de 100 iterações por padrão
- Requer chave de API da OpenAI válida
- Operações de escrita exigem `FileSystemProvider.ReadWrite`

## Próximos Passos

- Adicionar verificação de código
- Implementar execução de shell
- Melhorar tratamento de erros
- Adicionar suporte para mais LLMs

## Referências

Baseado no artigo: [Building AI Agents in Kotlin - Part 1: A Minimal Coding Agent](https://blog.jetbrains.com/ai/2025/11/building-ai-agents-in-kotlin-part-1-a-minimal-coding-agent/)
