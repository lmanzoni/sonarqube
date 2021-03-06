CREATE TABLE "ES_QUEUE" (
  "UUID" VARCHAR(40) NOT NULL PRIMARY KEY,
  "DOC_TYPE" VARCHAR(40) NOT NULL,
  "DOC_ID" VARCHAR(4000) NOT NULL,
  "DOC_ID_TYPE" VARCHAR(20),
  "DOC_ROUTING" VARCHAR(4000),
  "CREATED_AT" BIGINT NOT NULL
);
CREATE UNIQUE INDEX "PK_ES_QUEUE" ON "ES_QUEUE" ("UUID");
