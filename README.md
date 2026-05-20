# 🗺️ Where.io — Backend

> API REST para registro e visualização de lugares favoritos, com notas de visitas e sistema social entre amigos.

---

## 🚀 Sobre o projeto

O **Where.io** é uma aplicação que permite ao usuário salvar lugares que já visitou ou adora frequentar — restaurantes, bares, cafés e muito mais. Cada lugar pode ter fotos, avaliações, comentários e tags personalizadas. A plataforma também conta com um sistema social: você pode adicionar amigos e compartilhar descobertas. Este repositório contém o backend da aplicação, exposto como API REST.

---

## 🏗️ Arquitetura

O projeto segue a **Arquitetura Hexagonal (Ports & Adapters)**, onde toda a lógica de negócio vive na camada `application` e nunca depende de infraestrutura. As bordas da aplicação (HTTP, banco de dados, APIs externas) são implementadas como adaptadores que dependem de portas (interfaces), não o contrário. Isso garante que os casos de uso sejam testáveis de forma isolada e que trocar qualquer peça de infraestrutura não afete nenhuma regra de negócio.

**Camadas principais:**

| Camada | Responsabilidade |
|---|---|
| `adapters/in/web` | Controllers REST, DTOs e converters — entrada de dados via HTTP |
| `application/ports/in` | Interfaces de casos de uso (uma por operação) |
| `application/service` | Implementações dos casos de uso — toda a regra de negócio |
| `application/ports/out` | Interfaces de repositórios e serviços externos |
| `adapters/out/persistence` | Repositórios MongoDB, entities e mappers (MapStruct) |
| `adapters/out/external` | Integração com Google Places / Geocoding API |
| `adapters/out/auth` | Autenticação JWT e refresh token |

> 💡 Nenhuma classe em `application/` importa classes de `adapters/` — a dependência sempre flui de fora para dentro.

---

## 🛠️ Tecnologias

| Tecnologia | Versão | Função |
|---|---|---|
| ☕ Java | 17 | Linguagem principal |
| 🍃 Spring Boot | 4.0.1 | Framework web e IoC |
| 🔒 Spring Security + JWT | JJWT 0.12.6 | Autenticação e autorização |
| 🍃 MongoDB | latest | Banco de dados principal |
| 🗺️ MapStruct | 1.5.5 | Mapeamento compile-time entre camadas |
| 📦 Lombok | 1.18.30 | Redução de boilerplate |
| 📊 Actuator + Micrometer + Prometheus | — | Observabilidade e métricas |
| 📖 SpringDoc OpenAPI | 3.0.0 | Documentação Swagger automática |

> 🌐 Integração com **Google Places API** para autocomplete e geocoding de endereços.

---

## ⚙️ Pré-requisitos

Antes de começar, você precisa ter instalado:

- ☕ **Java 17+**
- 🐳 **MongoDB** rodando localmente na porta `27017`

---

## 🏃 Como inicializar

```bash
# Clone o repositório
git clone <url-do-repo>
cd where-io/backend

# Build sem rodar os testes
./mvnw clean install -DskipTests

# Sobe a aplicação
./mvnw spring-boot:run
```

> 🌐 A aplicação sobe em `http://localhost:8080` por padrão.
>
> 📖 Swagger UI disponível em `http://localhost:8080/swagger-ui.html`

---

## 📁 Estrutura do projeto

```
src/main/java/analu/whereio/
├── adapters/
│   ├── in/web/          # Controllers, DTOs, converters
│   └── out/
│       ├── auth/        # JWT adapter, refresh token
│       ├── external/    # Google Places / Geocoding
│       └── persistence/ # Entities, repositories, mappers
├── application/
│   ├── model/           # Modelos de domínio
│   ├── ports/
│   │   ├── in/          # Interfaces de casos de uso
│   │   └── out/         # Interfaces de repositórios e serviços
│   └── service/         # Implementações dos casos de uso
├── config/              # Beans Spring (WebClient, CORS, MongoDB)
└── exceptions/          # BusinessException + GlobalExceptionHandler
```

---

## 📬 Endpoints principais

### 📍 Locais
| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/local` | Cadastrar um local |
| `PUT` | `/api/local/{id}` | Atualizar um local |
| `DELETE` | `/api/local/{id}` | Remover um local |
| `GET` | `/api/local/all` | Listar todos os locais |
| `GET` | `/api/local/buscar-local` | Busca via Google Places autocomplete |

### 📝 Visitas
| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/visita` | Registrar uma visita |
| `PUT` | `/api/visita/{id}` | Atualizar uma visita |
| `DELETE` | `/api/visita/{id}` | Remover uma visita |
| `GET` | `/api/visita/{idLocal}` | Listar visitas de um local |

### 👥 Amigos
| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/amigos/convite` | Enviar convite de amizade |
| `PUT` | `/api/amigos/aceitar/{id}` | Aceitar convite |
| `GET` | `/api/amigos` | Listar amigos |

### 🔐 Autenticação
| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/auth/register` | Criar conta |
| `POST` | `/api/auth/login` | Login (retorna access + refresh token) |
| `POST` | `/api/auth/refresh` | Renovar access token |

---

## 📊 Observabilidade

Métricas expostas via Prometheus em `/actuator/prometheus`. Health check disponível em `/actuator/health`.
