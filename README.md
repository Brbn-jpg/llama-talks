# LlamaTalks

LlamaTalks is a Spring Boot-based chat application leveraging LangChain4j and Ollama for advanced conversational AI with Retrieval-Augmented Generation (RAG) capabilities. It supports streaming responses, conversation management, document ingestion, and persistent chat history.

## Features

- **Conversational AI**: Powered by Ollama and LangChain4j.
- **Retrieval-Augmented Generation (RAG)**: Ingest and query documents to provide context-aware responses.
- **Streaming Responses**: Real-time AI message streaming.
- **Conversation Management**: Start, retrieve, update, and delete conversations.
- **Document Ingestion**: Upload and process documents for semantic search.
- **Persistent History**: Stores all messages, conversations, and document embeddings in a database.
- **RESTful API**: Easy integration with frontends or other services.
- **Flexible Model Selection**: Support for multiple chat and embedding models from Ollama.

## Architecture

- **Spring Boot**: Backend framework.
- **LangChain4j**: AI orchestration, memory management, and RAG pipeline.
- **Ollama**: Local LLM inference with support for both chat and embedding models.
- **JPA/Hibernate**: Data persistence for conversations and messages.
- **Vector Store**: Semantic search capabilities using document embeddings.
- **Reactor/Flux**: Streaming support for real-time responses.

```
+-------------------+     +-------------------+     +-------------------+
|   Frontend/App    | <-> |   OllamaChat API  | <-> |   Ollama Server   |
+-------------------+     +-------------------+     +-------------------+
                                    |
                                    v
                          +-------------------+
                          |    Vector Store   |
                          |    (Embeddings)   |
                          +-------------------+
                                    |
                                    v
                          +-------------------+
                          |   PostgreSQL DB   |
                          +-------------------+
```

## Model Selection

### Chat Models

LlamaTalks supports various chat models available through Ollama. The choice of model affects response quality, speed, and resource usage.

#### Recommended Chat Models

| Model          | Size        | RAM Required | Use Case                | Speed  |
| -------------- | ----------- | ------------ | ----------------------- | ------ |
| `qwen2.5:0.5b` | 0.5B params | ~1GB         | Fast responses, testing | ⚡⚡⚡ |
| `qwen2.5:3b`   | 3B params   | ~3GB         | Balanced quality/speed  | ⚡⚡   |
| `llama3.2:3b`  | 3B params   | ~3GB         | General conversation    | ⚡⚡   |
| `phi3:3.8b`    | 3.8B params | ~4GB         | Coding, reasoning       | ⚡⚡   |
| `mistral:7b`   | 7B params   | ~8GB         | High-quality responses  | ⚡     |
| `llama3.1:8b`  | 8B params   | ~8GB         | Advanced reasoning      | ⚡     |
| `qwen2.5:14b`  | 14B params  | ~16GB        | Production quality      | 🐌     |
| `llama3.1:70b` | 70B params  | ~64GB        | Maximum quality         | 🐌🐌   |

#### Downloading Chat Models

```bash
# Lightweight (for development/testing)
ollama pull qwen2.5:0.5b

# Medium (recommended for production)
ollama pull mistral:7b
ollama pull llama3.1:8b

# Advanced (requires more resources)
ollama pull qwen2.5:14b
ollama pull llama3.1:70b

# Specialized models
ollama pull codellama:7b        # For code-related tasks
ollama pull dolphin-mixtral:8x7b # For complex reasoning
```

### Embedding Models

Embedding models are crucial for RAG functionality. They convert text into vector representations for semantic search.

#### Recommended Embedding Models

| Model                      | Dimensions | Use Case                  | Performance      |
| -------------------------- | ---------- | ------------------------- | ---------------- |
| `nomic-embed-text`         | 768        | General purpose, balanced | ⚡⚡ Recommended |
| `mxbai-embed-large`        | 1024       | High-quality embeddings   | ⚡               |
| `all-minilm`               | 384        | Fast, lightweight         | ⚡⚡⚡           |
| `snowflake-arctic-embed-l` | 1024       | Retrieval-focused         | ⚡⚡             |
| `bge-large`                | 1024       | Multilingual support      | ⚡               |

#### Important: Dimension Matching

**The `DIMENSIONS` value in `.env` must match your embedding model's output dimensions!**

```env
# For nomic-embed-text
DIMENSIONS=768

# For mxbai-embed-large or snowflake-arctic-embed
DIMENSIONS=1024

# For all-minilm
DIMENSIONS=384
```

#### Downloading Embedding Models

```bash
# Default (recommended)
ollama pull nomic-embed-text

# Alternatives
ollama pull mxbai-embed-large
ollama pull all-minilm
ollama pull snowflake-arctic-embed
```

### Configuration Examples

#### Fast & Lightweight Setup

```env
EMBEDDING_MODEL="all-minilm"
DIMENSIONS=384
CHAT_MODEL="qwen2.5:0.5b"
```

**Best for**: Development, testing, limited resources

#### Balanced Setup (Recommended)

```env
EMBEDDING_MODEL="nomic-embed-text"
DIMENSIONS=768
CHAT_MODEL="mistral:7b"
```

**Best for**: Production environments with moderate resources

#### High-Quality Setup

```env
EMBEDDING_MODEL="mxbai-embed-large"
DIMENSIONS=1024
CHAT_MODEL="llama3.1:8b"
```

**Best for**: Production environments requiring best quality

#### Maximum Performance Setup

```env
EMBEDDING_MODEL="snowflake-arctic-embed"
DIMENSIONS=1024
CHAT_MODEL="qwen2.5:14b"
```

**Best for**: High-end servers, enterprise deployments

### Viewing Available Models

```bash
# List all downloaded models
ollama list

# Search for available models
ollama search llama
ollama search embed
```

### Model Performance Considerations

#### Chat Models

- **Small models (< 3B)**: Fast responses, lower quality, minimal RAM
- **Medium models (3B-8B)**: Balanced performance, suitable for most use cases
- **Large models (> 8B)**: Best quality, slower responses, high RAM requirements

#### Embedding Models

- **Smaller dimensions (384)**: Faster processing, less storage, slightly lower accuracy
- **Medium dimensions (768)**: Balanced accuracy and performance
- **Larger dimensions (1024+)**: Better semantic understanding, more storage required

### Switching Models

To change models after initial setup:

1. Pull the new model:

   ```bash
   ollama pull new-model-name
   ```

2. Update `.env`:

   ```env
   CHAT_MODEL="new-model-name"
   # or
   EMBEDDING_MODEL="new-embedding-model"
   DIMENSIONS=xxx  # Update if embedding model changed
   ```

3. Restart the application:
   ```bash
   docker compose down
   docker compose up
   ```

**⚠️ Warning**: Changing the embedding model requires clearing database and re-ingesting all documents, as embeddings are not compatible between different models.

## RAG (Retrieval-Augmented Generation)

Llama-talks implements RAG to enhance AI responses with contextual information from your documents:

### How RAG Works

1. **Document Ingestion**: Upload documents through the `/ingestion` endpoint.
2. **Text Extraction**: Documents are parsed and split into semantic chunks using ApacheTikaDocumentParser.
3. **Embedding Generation**: Each chunk is converted into vector embeddings using your configured embedding model (e.g., `nomic-embed-text`).
4. **Vector Storage**: Embeddings are stored in a vector database for efficient similarity search.
5. **Query Enhancement**: When you ask a question, relevant document chunks are retrieved based on semantic similarity.
6. **Context-Aware Response**: The LLM generates responses using both the conversation history and retrieved document context.

### Benefits

- **Accurate Information**: Responses grounded in your specific documents and data.
- **Domain Knowledge**: Train the LlamaTalks on internal documentation, manuals, or knowledge bases.
- **Reduced Hallucinations**: LLM responses are backed by actual document content.
- **Scalable Knowledge**: Add new documents without retraining the model.

### Supported Document Formats

#### Be aware, that I did not test all of them. Video, audio and image files probably don't work with RAG as of this version

ApacheTikaDocumentParser can automatically detect and parse over 1,400 different file types, including:

#### Office Documents

- Microsoft Word: `.doc`, `.docx`
- Microsoft Excel: `.xls`, `.xlsx`
- Microsoft PowerPoint: `.ppt`, `.pptx`
- OpenOffice/LibreOffice: `.odt`, `.ods`, `.odp`
- WordPerfect: `.wpd` (WP6+)
- QuattroPro: `.qpw` (v9+)

#### Text and Markup

- Plain Text: `.txt`
- Rich Text Format: `.rtf`
- Markdown: `.md`
- HTML/XHTML: `.html`, `.htm`
- XML: `.xml`

#### PDF Documents

- Portable Document Format: `.pdf`
- E-books: `.epub`, `.fb2` (FictionBook)

#### Images

- JPEG: `.jpg`, `.jpeg` (with EXIF metadata)
- TIFF: `.tiff`, `.tif`
- PNG: `.png`
- GIF: `.gif`
- BMP: `.bmp`
- WebP: `.webp`
- PSD: `.psd` (Adobe Photoshop)
- ICNS: `.icns` (Apple icon format)
- BPG: `.bpg` (Better Portable Graphics)

#### Audio Files

- MP3: `.mp3`
- MP4 Audio: `.m4a`
- MIDI: `.mid`, `.midi`
- WAV: `.wav`
- OGG: `.ogg`
- FLAC: `.flac`

#### Video Files

- MP4: `.mp4`
- AVI: `.avi`
- FLV: `.flv` (Flash Video)
- MOV: `.mov`

#### Archives and Compression

- ZIP: `.zip`
- TAR: `.tar`
- GZIP: `.gz`
- BZIP2: `.bz2`
- 7-Zip: `.7z`
- RAR: `.rar`

#### Other Formats

- RSS/Atom Feeds: `.rss`, `.atom`
- CHM: `.chm` (Compiled HTML Help)
- IPTC ANPA: News wire format
- Microsoft Drawings: `.wmf`, `.emf`

**Note**: Apache Tika can also perform OCR (Optical Character Recognition) on images using Tesseract to extract text from scanned documents and images.

## Setup

### Prerequisites

- Docker
- Ollama

### If you are running nvidia gpu

Don't download Ollama locally. Instead uncomment everything in `docker-compose.yml` and change env file OLLAMA_URL from `http://host.docker.internal:11434` to `http://ollama:11434` - this way you can skip step 2.

#### 1. Create .env file

```env
POSTGRES_DB="databaseName"
POSTGRES_USER="databaseUser"
POSTGRES_PASSWORD="databasePassword"
DB_URL="jdbc:postgresql://database:5432/databaseName"
OLLAMA_URL="http://host.docker.internal:11434"
DIMENSIONS=768
EMBEDDING_MODEL="nomic-embed-text"
CHAT_MODEL="qwen2.5:0.5b"
```

**Important**: Make sure `DIMENSIONS` matches your embedding model's output dimensions! See [Model Selection](#model-selection) for details.

#### 2. Download ollama models

```bash
ollama serve

# Download your chosen embedding model (dimensions must match .env)
ollama pull nomic-embed-text

# Download your chosen chat model
ollama pull qwen2.5:0.5b

# Optional: Download alternative models for testing
ollama pull mistral:7b
ollama pull all-minilm
```

**Note**: See [Model Selection](#model-selection) section for recommendations on which models to use.

#### 3. Run docker

```bash
docker compose build
```

After it builds:

```bash
docker compose up
```

or if you don't want logs:

```bash
docker compose up -d
```

## Usage

### REST Endpoints

#### 1. Chat

- **POST** `/chat`
- **Body**:
  ```json
  {
    "message": "Hello!",
    "conversationId": "optional-uuid"
  }
  ```
- **Response**:
  ```json
  {
    "response": "Hi! How can I help you?",
    "conversationId": "uuid"
  }
  ```

#### 2. Stream Chat

- **POST** `/chat/stream`
- **Body**: Same as above
- **Response**: Server-Sent Events (SSE) stream of AI response

#### 3. Get All Conversations

- **GET** `/conversations`
- **Response**: List of conversations

#### 4. Get Conversation by ID

- **GET** `/conversations/{conversationId}`

#### 5. Change Conversation Title

- **PUT** `/conversations/{conversationId}`
- **Body**:
  ```json
  {
    "title": "New Title"
  }
  ```

#### 6. Delete Conversation

- **DELETE** `/conversations/{conversationId}`

#### 7. Ingest Documents (RAG)

- **POST** `/ingestion?filePath=files/`

#### 8. Get All Ingested Documents

- **GET** `/ingestion`
- **Response**: List of ingested documents with metadata

## Example Requests

### Chat Request (cURL)

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Hello","conversationId":""}'
```

### Document Ingestion (cURL)

```bash
curl -X POST "http://localhost:8080/api/ingestion?filePath=files/" \
  -H "Content-Type: application/json"
```

### Stream Chat with RAG Context

```bash
curl -X POST http://localhost:8080/api/chat/stream \
  -H "Content-Type: application/json" \
  -d '{"message":"What does the documentation say about installation?","conversationId":""}' \
  --no-buffer
```

## RAG Use Cases

### 1. Technical Documentation Assistant

Ingest your project's documentation and let users ask questions about APIs, configurations, and best practices.

### 2. Customer Support

Upload product manuals, FAQs, and support articles to provide instant, accurate customer assistance.

### 3. Internal Knowledge Base

Index company policies, procedures, and training materials for employee self-service.

### 4. Research Assistant

Process research papers, articles, and reports to answer domain-specific questions.

### 5. Legal Document Analysis

Upload contracts, regulations, and legal documents for contextual Q&A.

## Contributing

1. Fork the repo
2. Create a feature branch
3. Commit changes with clear messages
4. Open a PR

## Troubleshooting

- **Ollama not running**: Ensure `ollama serve` is active.
- **Model not found**: Run `ollama pull <model-name>` for your chosen chat and embedding models.
- **Database errors**: Check DB configuration in `application.yml`.
- **Timeouts**: Increase timeout in `ChatServiceImpl`.
- **RAG not working**: Verify that documents are successfully ingested and embeddings are generated.
- **Slow document processing**: Large documents may take time to chunk and embed; consider pagination for large files.
- **Out of memory**: Adjust JVM heap size or reduce embedding batch size for large document sets. Consider using a smaller model.
- **Unsupported file format**: Check if the file type is among the supported formats listed above.
- **Dimension mismatch error**: Ensure `DIMENSIONS` in `.env` matches your embedding model's output dimensions.
- **Slow chat responses**: Consider switching to a smaller/faster chat model. See [Model Selection](#model-selection).

## Performance Optimization

### For RAG

- **Chunk Size**: Adjust document chunking parameters for optimal context length.
- **Embedding Cache**: Embeddings are cached to avoid recomputation.
- **Vector Search**: Configure the number of retrieved chunks (top-k) based on your use case.
- **Model Selection**: Use larger embedding models for better semantic understanding, or smaller ones for speed.

### Model-Specific Optimization

- **RAM Management**: Monitor RAM usage and switch to smaller models if experiencing performance issues.
- **GPU Acceleration**: If using NVIDIA GPU (uncommented in docker-compose), ensure models fit in VRAM.
- **Concurrent Requests**: Smaller models handle concurrent requests better than larger ones.
- **Response Time**: Balance quality vs. speed by choosing appropriate model size for your use case.

## License

MIT License
