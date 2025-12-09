/**
 * ============================================================================
 * MONGODB SCHEMAS - DEVELOPER PORTAL
 * ============================================================================
 *
 * Este archivo contiene TODOS los schemas de MongoDB para el Developer Portal
 * Incluye:
 * - 8 Collections HÍBRIDAS (conectadas a tablas SQL)
 * - 2 Collections STANDALONE (solo MongoDB)
 *
 * Ejecutar en MongoDB Shell:
 * mongosh mongodb://localhost:27017/dev_portal < mongodb_schemas.js
 * ============================================================================
 */

// Conectar a la base de datos
// Note: Database connection should be handled by your MongoDB driver/client
// use dev_portal; // This line is for MongoDB shell only

print("🚀 Inicializando schemas de MongoDB para Developer Portal...\n");

// ============================================================================
// PARTE 1: COLLECTIONS HÍBRIDAS (conectadas a SQL)
// ============================================================================

print("📊 PARTE 1: Creando 8 collections híbridas...\n");

// -----------------------------------------------------------------------------
// 1. test_case_config (híbrida con api_test_case)
// -----------------------------------------------------------------------------
print("1️⃣  Creando collection: test_case_config");

db.createCollection("test_case_config", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["test_case_id", "request"],
            properties: {
                test_case_id: {
                    bsonType: "long",
                    description: "ID de la tabla SQL api_test_case (REQUIRED)"
                },
                request: {
                    bsonType: "object",
                    required: ["method", "endpoint"],
                    properties: {
                        method: {
                            bsonType: "string",
                            enum: ["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"]
                        },
                        endpoint: {
                            bsonType: "string"
                        },
                        headers: {
                            bsonType: "object"
                        },
                        query_params: {
                            bsonType: "object"
                        },
                        body: {
                            bsonType: ["object", "string", "null"]
                        }
                    }
                },
                validations: {
                    bsonType: "array",
                    items: {
                        bsonType: "object",
                        required: ["type"],
                        properties: {
                            type: {
                                bsonType: "string",
                                enum: ["status_code", "json_path", "json_schema", "response_time", "custom_script"]
                            },
                            expected: {},
                            path: { bsonType: "string" },
                            script: { bsonType: "string" }
                        }
                    }
                },
                pre_request_script: {
                    bsonType: ["string", "null"]
                },
                post_response_script: {
                    bsonType: ["string", "null"]
                },
                created_at: {
                    bsonType: "date"
                },
                updated_at: {
                    bsonType: "date"
                }
            }
        }
    }
});

db.test_case_config.createIndex({ "test_case_id": 1 }, { unique: true });
db.test_case_config.createIndex({ "created_at": -1 });

print("   ✅ Collection test_case_config creada\n");

// -----------------------------------------------------------------------------
// 2. test_log_detalle (híbrida con api_test_log)
// -----------------------------------------------------------------------------
print("2️⃣  Creando collection: test_log_detalle");

db.createCollection("test_log_detalle", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["test_log_id", "request", "response"],
            properties: {
                test_log_id: {
                    bsonType: "long",
                    description: "ID de la tabla SQL api_test_log (REQUIRED)"
                },
                request: {
                    bsonType: "object",
                    properties: {
                        url: { bsonType: "string" },
                        method: { bsonType: "string" },
                        headers: { bsonType: "object" },
                        body: {},
                        size_bytes: { bsonType: "int" },
                        timestamp: { bsonType: "date" }
                    }
                },
                response: {
                    bsonType: "object",
                    properties: {
                        status_code: { bsonType: "int" },
                        headers: { bsonType: "object" },
                        body: {},
                        size_bytes: { bsonType: "int" },
                        time_ms: { bsonType: "int" },
                        timestamp: { bsonType: "date" }
                    }
                },
                validation_results: {
                    bsonType: "array",
                    items: {
                        bsonType: "object",
                        properties: {
                            validation_type: { bsonType: "string" },
                            passed: { bsonType: "bool" },
                            expected: {},
                            actual: {},
                            error_message: { bsonType: ["string", "null"] }
                        }
                    }
                },
                console_output: {
                    bsonType: ["string", "null"]
                },
                error_stack: {
                    bsonType: ["string", "null"]
                },
                created_at: {
                    bsonType: "date"
                },
                expireAt: {
                    bsonType: ["date", "null"],
                    description: "TTL: Eliminar después de 90 días"
                }
            }
        }
    }
});

db.test_log_detalle.createIndex({ "test_log_id": 1 }, { unique: true });
db.test_log_detalle.createIndex({ "expireAt": 1 }, { expireAfterSeconds: 0 });
db.test_log_detalle.createIndex({ "created_at": -1 });

print("   ✅ Collection test_log_detalle creada\n");

// -----------------------------------------------------------------------------
// 3. mock_server_config (híbrida con api_mock_server)
// -----------------------------------------------------------------------------
print("3️⃣  Creando collection: mock_server_config");

db.createCollection("mock_server_config", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["mock_server_id", "endpoints"],
            properties: {
                mock_server_id: {
                    bsonType: "long",
                    description: "ID de la tabla SQL api_mock_server (REQUIRED)"
                },
                global_headers: {
                    bsonType: "object"
                },
                endpoints: {
                    bsonType: "array",
                    minItems: 1,
                    items: {
                        bsonType: "object",
                        required: ["method", "path", "response"],
                        properties: {
                            method: {
                                bsonType: "string",
                                enum: ["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"]
                            },
                            path: {
                                bsonType: "string"
                            },
                            response: {
                                bsonType: "object",
                                required: ["status_code"],
                                properties: {
                                    status_code: { bsonType: "int" },
                                    headers: { bsonType: "object" },
                                    body_template: {},
                                    delay_ms: { bsonType: "int" }
                                }
                            },
                            conditions: {
                                bsonType: ["array", "null"],
                                items: {
                                    bsonType: "object",
                                    properties: {
                                        condition: { bsonType: "string" },
                                        response_override: { bsonType: "object" }
                                    }
                                }
                            }
                        }
                    }
                },
                fallback_response: {
                    bsonType: "object",
                    properties: {
                        status_code: { bsonType: "int" },
                        body: {}
                    }
                },
                rules: {
                    bsonType: "array",
                    items: {
                        bsonType: "object",
                        properties: {
                            condition: { bsonType: "string" },
                            response: { bsonType: "object" }
                        }
                    }
                },
                created_at: {
                    bsonType: "date"
                },
                updated_at: {
                    bsonType: "date"
                }
            }
        }
    }
});

db.mock_server_config.createIndex({ "mock_server_id": 1 }, { unique: true });
db.mock_server_config.createIndex({ "created_at": -1 });

print("   ✅ Collection mock_server_config creada\n");

// -----------------------------------------------------------------------------
// 4. file_metadata (híbrida con enlace)
// -----------------------------------------------------------------------------
print("4️⃣  Creando collection: file_metadata");

db.createCollection("file_metadata", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["enlace_id", "file_metadata"],
            properties: {
                enlace_id: {
                    bsonType: "long",
                    description: "ID de la tabla SQL enlace (REQUIRED)"
                },
                file_metadata: {
                    bsonType: "object",
                    required: ["size_bytes", "mime_type"],
                    properties: {
                        size_bytes: { bsonType: "long" },
                        checksum_sha256: { bsonType: ["string", "null"] },
                        checksum_md5: { bsonType: ["string", "null"] },
                        mime_type: { bsonType: "string" },
                        encoding: { bsonType: ["string", "null"] },
                        // Para videos
                        duration_seconds: { bsonType: ["int", "null"] },
                        resolution: { bsonType: ["string", "null"] },
                        bitrate: { bsonType: ["int", "null"] },
                        // Para imágenes
                        width: { bsonType: ["int", "null"] },
                        height: { bsonType: ["int", "null"] },
                        format: { bsonType: ["string", "null"] },
                        // Para audio
                        sample_rate: { bsonType: ["int", "null"] },
                        channels: { bsonType: ["int", "null"] }
                    }
                },
                versions: {
                    bsonType: "array",
                    items: {
                        bsonType: "object",
                        required: ["version", "url", "uploaded_at"],
                        properties: {
                            version: { bsonType: "int" },
                            url: { bsonType: "string" },
                            uploaded_at: { bsonType: "date" },
                            size_bytes: { bsonType: "long" },
                            changes: { bsonType: ["string", "null"] },
                            uploaded_by: { bsonType: ["long", "null"] }
                        }
                    }
                },
                access_log: {
                    bsonType: "array",
                    items: {
                        bsonType: "object",
                        properties: {
                            usuario_id: { bsonType: "long" },
                            fecha: { bsonType: "date" },
                            ip: { bsonType: "string" }
                        }
                    }
                },
                tags: {
                    bsonType: "array",
                    items: { bsonType: "string" }
                },
                created_at: {
                    bsonType: "date"
                },
                expireAt: {
                    bsonType: ["date", "null"],
                    description: "TTL: Para archivos temporales"
                }
            }
        }
    }
});

db.file_metadata.createIndex({ "enlace_id": 1 }, { unique: true });
db.file_metadata.createIndex({ "expireAt": 1 }, { expireAfterSeconds: 0 });
db.file_metadata.createIndex({ "file_metadata.mime_type": 1 });
db.file_metadata.createIndex({ "file_metadata.size_bytes": -1 });
db.file_metadata.createIndex({ "tags": 1 });

print("   ✅ Collection file_metadata creada\n");

// -----------------------------------------------------------------------------
// 5. audit_snapshots (híbrida con historial)
// -----------------------------------------------------------------------------
print("5️⃣  Creando collection: audit_snapshots");

db.createCollection("audit_snapshots", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["historial_id", "entity_type"],
            properties: {
                historial_id: {
                    bsonType: "long",
                    description: "ID de la tabla SQL historial (REQUIRED)"
                },
                entity_type: {
                    bsonType: "string",
                    description: "Tipo de entidad (api, proyecto, usuario, etc.)"
                },
                entity_id: {
                    bsonType: "long",
                    description: "ID de la entidad afectada"
                },
                before: {
                    bsonType: ["object", "null"],
                    description: "Snapshot COMPLETO antes del cambio"
                },
                after: {
                    bsonType: ["object", "null"],
                    description: "Snapshot COMPLETO después del cambio"
                },
                diff: {
                    bsonType: "object",
                    properties: {
                        added_fields: { bsonType: "array" },
                        modified_fields: { bsonType: "array" },
                        deleted_fields: { bsonType: "array" },
                        added_relations: { bsonType: "array" },
                        deleted_relations: { bsonType: "array" }
                    }
                },
                metadata: {
                    bsonType: "object",
                    properties: {
                        user_agent: { bsonType: "string" },
                        ip: { bsonType: "string" },
                        geolocation: { bsonType: ["string", "null"] },
                        session_id: { bsonType: ["string", "null"] }
                    }
                },
                created_at: {
                    bsonType: "date"
                },
                expireAt: {
                    bsonType: ["date", "null"],
                    description: "TTL: Eliminar auditorías después de 5 años"
                }
            }
        }
    }
});

db.audit_snapshots.createIndex({ "historial_id": 1 }, { unique: true });
db.audit_snapshots.createIndex({ "entity_type": 1, "entity_id": 1 });
db.audit_snapshots.createIndex({ "expireAt": 1 }, { expireAfterSeconds: 0 });
db.audit_snapshots.createIndex({ "created_at": -1 });

print("   ✅ Collection audit_snapshots creada\n");

// -----------------------------------------------------------------------------
// 6. chatbot_messages (híbrida con chatbot_conversacion)
// -----------------------------------------------------------------------------
print("6️⃣  Creando collection: chatbot_messages");

db.createCollection("chatbot_messages", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["conversacion_id", "mensajes"],
            properties: {
                conversacion_id: {
                    bsonType: "long",
                    description: "ID de la tabla SQL chatbot_conversacion (REQUIRED)"
                },
                mensajes: {
                    bsonType: "array",
                    items: {
                        bsonType: "object",
                        required: ["mensaje_id", "remitente", "contenido", "timestamp"],
                        properties: {
                            mensaje_id: { bsonType: "int" },
                            remitente: {
                                bsonType: "string",
                                enum: ["USUARIO", "IA"]
                            },
                            contenido: { bsonType: "string" },
                            timestamp: { bsonType: "date" },
                            tokens: { bsonType: "int" },
                            // Solo para mensajes de IA
                            modelo: { bsonType: ["string", "null"] },
                            confidence: { bsonType: ["double", "null"] },
                            referencias: {
                                bsonType: ["array", "null"],
                                items: {
                                    bsonType: "object",
                                    properties: {
                                        tipo: { bsonType: "string" },
                                        id: { bsonType: "long" },
                                        titulo: { bsonType: "string" },
                                        url: { bsonType: ["string", "null"] }
                                    }
                                }
                            }
                        }
                    }
                },
                context_window: {
                    bsonType: "array",
                    description: "Últimos N mensajes para mantener contexto en IA",
                    items: {
                        bsonType: "object",
                        properties: {
                            role: { bsonType: "string", enum: ["user", "assistant", "system"] },
                            content: { bsonType: "string" }
                        }
                    }
                },
                embeddings: {
                    bsonType: ["array", "null"],
                    description: "Embeddings de mensajes para búsqueda semántica",
                    items: {
                        bsonType: "object",
                        properties: {
                            mensaje_id: { bsonType: "int" },
                            embedding: { bsonType: "array", items: { bsonType: "double" } }
                        }
                    }
                },
                metadata: {
                    bsonType: "object",
                    properties: {
                        total_tokens: { bsonType: "int" },
                        last_ia_model: { bsonType: "string" },
                        sentiment: { bsonType: ["string", "null"] }
                    }
                },
                created_at: {
                    bsonType: "date"
                },
                updated_at: {
                    bsonType: "date"
                },
                expireAt: {
                    bsonType: ["date", "null"],
                    description: "TTL: Eliminar después de 90 días"
                }
            }
        }
    }
});

db.chatbot_messages.createIndex({ "conversacion_id": 1 }, { unique: true });
db.chatbot_messages.createIndex({ "expireAt": 1 }, { expireAfterSeconds: 0 });
db.chatbot_messages.createIndex({ "updated_at": -1 });

print("   ✅ Collection chatbot_messages creada\n");

// -----------------------------------------------------------------------------
// 7. notification_payload (híbrida con notificacion)
// -----------------------------------------------------------------------------
print("7️⃣  Creando collection: notification_payload");

db.createCollection("notification_payload", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["notificacion_id", "payload"],
            properties: {
                notificacion_id: {
                    bsonType: "long",
                    description: "ID de la tabla SQL notificacion (REQUIRED)"
                },
                payload: {
                    bsonType: "object",
                    required: ["titulo", "mensaje"],
                    properties: {
                        titulo: { bsonType: "string" },
                        mensaje: { bsonType: "string" },
                        usuario_origen: {
                            bsonType: ["object", "null"],
                            properties: {
                                usuario_id: { bsonType: "long" },
                                nombre: { bsonType: "string" },
                                avatar_url: { bsonType: ["string", "null"] }
                            }
                        },
                        entidad_relacionada: {
                            bsonType: ["object", "null"],
                            properties: {
                                tipo: { bsonType: "string" },
                                id: { bsonType: "long" },
                                nombre: { bsonType: "string" },
                                url: { bsonType: ["string", "null"] }
                            }
                        },
                        accion: {
                            bsonType: ["object", "null"],
                            properties: {
                                texto: { bsonType: "string" },
                                url: { bsonType: "string" },
                                tipo: { bsonType: "string", enum: ["PRIMARY", "SECONDARY", "LINK"] }
                            }
                        },
                        metadata: {
                            bsonType: "object",
                            properties: {
                                icono: { bsonType: ["string", "null"] },
                                color: { bsonType: ["string", "null"] },
                                imagen_preview: { bsonType: ["string", "null"] }
                            }
                        }
                    }
                },
                created_at: {
                    bsonType: "date"
                },
                expireAt: {
                    bsonType: ["date", "null"],
                    description: "TTL: Eliminar después de 90 días"
                }
            }
        }
    }
});

db.notification_payload.createIndex({ "notificacion_id": 1 }, { unique: true });
db.notification_payload.createIndex({ "expireAt": 1 }, { expireAfterSeconds: 0 });
db.notification_payload.createIndex({ "created_at": -1 });

print("   ✅ Collection notification_payload creada\n");

// -----------------------------------------------------------------------------
// 8. feedback_details (híbrida con feedback)
// -----------------------------------------------------------------------------
print("8️⃣  Creando collection: feedback_details");

db.createCollection("feedback_details", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["feedback_id"],
            properties: {
                feedback_id: {
                    bsonType: "long",
                    description: "ID de la tabla SQL feedback (REQUIRED)"
                },
                adjuntos: {
                    bsonType: "array",
                    items: {
                        bsonType: "object",
                        required: ["tipo", "nombre"],
                        properties: {
                            tipo: {
                                bsonType: "string",
                                enum: ["SCREENSHOT", "VIDEO", "LOG", "DOCUMENTO", "OTRO"]
                            },
                            nombre: { bsonType: "string" },
                            url: { bsonType: ["string", "null"] },
                            contenido: { bsonType: ["string", "null"] },
                            size_bytes: { bsonType: ["long", "null"] },
                            mime_type: { bsonType: ["string", "null"] }
                        }
                    }
                },
                metadata_navegador: {
                    bsonType: "object",
                    properties: {
                        user_agent: { bsonType: "string" },
                        resolucion: { bsonType: "string" },
                        navegador: { bsonType: "string" },
                        version_navegador: { bsonType: "string" },
                        sistema_operativo: { bsonType: "string" },
                        idioma: { bsonType: "string" }
                    }
                },
                reproduccion_bug: {
                    bsonType: ["object", "null"],
                    properties: {
                        pasos: {
                            bsonType: "array",
                            items: { bsonType: "string" }
                        },
                        esperado: { bsonType: "string" },
                        obtenido: { bsonType: "string" },
                        frecuencia: {
                            bsonType: "string",
                            enum: ["SIEMPRE", "A_VECES", "RARA_VEZ"]
                        }
                    }
                },
                datos_tecnicos: {
                    bsonType: "object",
                    properties: {
                        api_version: { bsonType: ["string", "null"] },
                        endpoint_afectado: { bsonType: ["string", "null"] },
                        errores_consola: {
                            bsonType: "array",
                            items: { bsonType: "string" }
                        },
                        network_tab: {
                            bsonType: "array",
                            items: {
                                bsonType: "object",
                                properties: {
                                    url: { bsonType: "string" },
                                    method: { bsonType: "string" },
                                    status: { bsonType: "int" },
                                    duration_ms: { bsonType: "int" }
                                }
                            }
                        }
                    }
                },
                created_at: {
                    bsonType: "date"
                }
            }
        }
    }
});

db.feedback_details.createIndex({ "feedback_id": 1 }, { unique: true });
db.feedback_details.createIndex({ "created_at": -1 });
db.feedback_details.createIndex({ "metadata_navegador.navegador": 1 });
db.feedback_details.createIndex({ "metadata_navegador.sistema_operativo": 1 });

print("   ✅ Collection feedback_details creada\n");

// ============================================================================
// PARTE 2: COLLECTIONS STANDALONE (solo MongoDB)
// ============================================================================

print("\n📊 PARTE 2: Creando 2 collections standalone...\n");

// -----------------------------------------------------------------------------
// 9. api_metrics (STANDALONE - time series)
// -----------------------------------------------------------------------------
print("9️⃣  Creando collection: api_metrics (time series)");

db.createCollection("api_metrics", {
    timeseries: {
        timeField: "timestamp",
        metaField: "metadata",
        granularity: "minutes"
    },
    expireAfterSeconds: 7776000  // TTL: 90 días
});

db.api_metrics.createIndex({ "metadata.api_id": 1, "timestamp": -1 });
db.api_metrics.createIndex({ "metadata.endpoint": 1, "timestamp": -1 });
db.api_metrics.createIndex({ "metadata.status_code": 1, "timestamp": -1 });

print("   ✅ Collection api_metrics creada (time series)\n");

// Insertar ejemplo
db.api_metrics.insertOne({
    timestamp: new Date(),
    metadata: {
        api_id: 42,
        version_id: 10,
        endpoint: "/api/payments/charge",
        method: "POST",
        usuario_id: 15
    },
    status_code: 200,
    response_time_ms: 250,
    request_size_bytes: 1024,
    response_size_bytes: 2048,
    error: null
});

print("   📝 Documento de ejemplo insertado\n");

// -----------------------------------------------------------------------------
// 10. system_logs (STANDALONE - logs generales)
// -----------------------------------------------------------------------------
print("🔟 Creando collection: system_logs");

db.createCollection("system_logs", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["timestamp", "level", "message"],
            properties: {
                timestamp: {
                    bsonType: "date"
                },
                level: {
                    bsonType: "string",
                    enum: ["DEBUG", "INFO", "WARN", "ERROR", "FATAL"]
                },
                message: {
                    bsonType: "string"
                },
                logger: {
                    bsonType: "string"
                },
                thread: {
                    bsonType: "string"
                },
                context: {
                    bsonType: "object"
                },
                stack_trace: {
                    bsonType: ["string", "null"]
                },
                usuario_id: {
                    bsonType: ["long", "null"]
                },
                request_id: {
                    bsonType: ["string", "null"]
                },
                expireAt: {
                    bsonType: ["date", "null"],
                    description: "TTL: Eliminar logs después de 30 días"
                }
            }
        }
    }
});

db.system_logs.createIndex({ "expireAt": 1 }, { expireAfterSeconds: 0 });
db.system_logs.createIndex({ "timestamp": -1 });
db.system_logs.createIndex({ "level": 1, "timestamp": -1 });
db.system_logs.createIndex({ "usuario_id": 1, "timestamp": -1 });
db.system_logs.createIndex({ "request_id": 1 });

print("   ✅ Collection system_logs creada\n");

// ============================================================================
// RESUMEN FINAL
// ============================================================================

print("\n" + "=".repeat(80));
print("✅ COMPLETADO: Schemas de MongoDB creados exitosamente");
print("=".repeat(80) + "\n");

print("📊 RESUMEN:");
print("   - Collections HÍBRIDAS: 8");
print("     1. test_case_config");
print("     2. test_log_detalle");
print("     3. mock_server_config");
print("     4. file_metadata");
print("     5. audit_snapshots");
print("     6. chatbot_messages");
print("     7. notification_payload");
print("     8. feedback_details");
print("");
print("   - Collections STANDALONE: 2");
print("     9. api_metrics (time series)");
print("     10. system_logs");
print("");
print("   - TOTAL: 10 collections\n");

print("🔍 Verificación:");
db.getCollectionNames().forEach(function(col) {
    print("   ✓ " + col);
});

print("\n💡 PRÓXIMOS PASOS:");
print("   1. Configurar application.properties con MongoDB URI");
print("   2. Implementar repositories/services en Spring Boot");
print("   3. Configurar TTL indexes para expiración automática");
print("   4. Implementar validación de integridad entre SQL y MongoDB");
print("");

print("🚀 MongoDB está listo para el Developer Portal!\n");
