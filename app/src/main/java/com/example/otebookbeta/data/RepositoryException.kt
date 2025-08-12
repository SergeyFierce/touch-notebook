package com.example.otebookbeta.data

/**
 * Централизованное исключение репозитория — чтобы не "протекали" детали Room наружу.
 */
class RepositoryException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
