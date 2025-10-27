# v1

v1 is a Spring Boot-based LlamaTalks application leveraging LangChain4j and Ollama for advanced conversational AI with Retrieval-Augmented Generation (RAG) capabilities. It supports streaming responses, conversation management, document ingestion, and persistent chat history.

## Features

- **Conversational AI**: Powered by Ollama and LangChain4j.
- **Retrieval-Augmented Generation (RAG)**: Ingest and query documents to provide context-aware responses.
- **Streaming Responses**: Real-time AI message streaming.
- **Conversation Management**: Start, retrieve, update, and delete conversations.
- **Document Ingestion**: Upload and process documents for semantic search.
- **Persistent History**: Stores all messages, conversations, and document embeddings in a database.
- **RESTful API**: Easy integration with frontends or other services.

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

## RAG (Retrieval-Augmented Generation)

v1 implements RAG to enhance AI responses with contextual information from your documents:

### How RAG Works

1. **Document Ingestion**: Upload documents through the `/ingestion` endpoint.
2. **Text Extraction**: Documents are parsed and split into semantic chunks using ApacheTikaDocumentParser.
3. **Embedding Generation**: Each chunk is converted into vector embeddings using the `nomic-embed-text` model.
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
```

#### 2. Download ollama models

```bash
ollama serve
ollama pull qwen2.5:0.5b
ollama pull nomic-embed-text
```

**Note**: The `nomic-embed-text` model is essential for RAG functionality as it generates document embeddings.

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
- **Model not found**: Run `ollama pull qwen2.5:0.5b` and `ollama pull nomic-embed-text`.
- **Database errors**: Check DB configuration in `application.yml`.
- **Timeouts**: Increase timeout in `ChatServiceImpl`.
- **RAG not working**: Verify that documents are successfully ingested and embeddings are generated.
- **Slow document processing**: Large documents may take time to chunk and embed; consider pagination for large files.
- **Out of memory**: Adjust JVM heap size or reduce embedding batch size for large document sets.
- **Unsupported file format**: Check if the file type is among the supported formats listed above.

## Performance Optimization

### For RAG

- **Chunk Size**: Adjust document chunking parameters for optimal context length.
- **Embedding Cache**: Embeddings are cached to avoid recomputation.
- **Vector Search**: Configure the number of retrieved chunks (top-k) based on your use case.
- **Model Selection**: Use larger embedding models for better semantic understanding, or smaller ones for speed.
