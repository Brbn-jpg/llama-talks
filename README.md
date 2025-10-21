# OllamaChat

OllamaChat is a Spring Boot-based chatbot application leveraging LangChain4j and Ollama for advanced conversational AI. It supports streaming responses, conversation management, and persistent chat history.

## Features

- **Conversational AI**: Powered by Ollama and LangChain4j.
- **Streaming Responses**: Real-time AI message streaming.
- **Conversation Management**: Start, retrieve, update, and delete conversations.
- **Persistent History**: Stores all messages and conversations in a database.
- **RESTful API**: Easy integration with frontends or other services.

## Architecture

- **Spring Boot**: Backend framework.
- **LangChain4j**: AI and memory management.
- **Ollama**: Local LLM inference.
- **JPA/Hibernate**: Data persistence.
- **Reactor/Flux**: Streaming support.

```
+-------------------+       +-------------------+       +-------------------+
|   Frontend/App    | <---> |   OllamaChat API  | <---> |    Ollama Server  |
+-------------------+       +-------------------+       +-------------------+
```

## Setup

### Prerequisites

- Docker

#### 1. Create .env file

```
POSTGRES_DB="databaseName"
POSTGRES_USER="databaseUser"
POSTGRES_PASSWORD="databasePassword"
DB_URL="jdbc:postgresql://database:5432/databaseName"
```

#### 2. Run docker

`docker compose build`

After it builds

`docker compose up`
or if you dont want logs
`docker compose up -d`

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

- **PUT** `/conversations/{conversationId}/title`
- **Body**:
  ```json
  {
    "title": "New Title"
  }
  ```

#### 6. Delete Conversation

- **DELETE** `/conversations/{conversationId}`

## Example Request (cURL)

```sh
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Hello"}'
```

## Contributing

1. Fork the repo
2. Create a feature branch
3. Commit changes with clear messages
4. Open a PR

## Troubleshooting

- **Ollama not running**: Ensure `ollama serve` is active.
- **Model not found**: Run `ollama pull qwen2.5:0.5b`.
- **Database errors**: Check DB configuration in `application.yml`.
- **Timeouts**: Increase timeout in `ChatServiceImpl`.
