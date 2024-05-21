package com.orbys.vote.data.controllers.handlers

import com.orbys.vote.core.managers.NetworkManager.Companion.QUESTION_ENDPOINT
import com.orbys.vote.core.managers.NetworkManager.Companion.USER_ENDPOINT
import com.orbys.vote.data.repositories.QuestionRepositoryImpl
import com.orbys.vote.data.repositories.UsersRepositoryImpl
import com.orbys.vote.domain.models.User
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.plugins.origin
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import javax.inject.Inject

/**
 * Clase para gstionar las peticiones del servidor.
 *
 * @property questionRepository Repositorio de preguntas.
 * @property usersRepository Repositorio de usuarios.
 * @property fileHandler Gestor de archivos.
 */
class ResponseHandler@Inject constructor(
    private val questionRepository: QuestionRepositoryImpl,
    private val usersRepository: UsersRepositoryImpl,
    private val fileHandler: FileHandler
): IHttpHandler {

    override fun setupRoutes(route: Route) {
        route.apply {
            handleGetQuestionRoute()
            handleSubmitQuestionRoute()

            handleNewUserRoute()
            handleLoginRoute()
        }
    }

    /**
     * Ruta que muestra el formulario para contestar la pregunta.
     *
     * @return GET
     */
    private fun Route.handleGetQuestionRoute() = get(QUESTION_ENDPOINT) {
        val userIP = call.request.origin.remoteHost
        val question = questionRepository.getQuestion()
        val fileContent = fileHandler.loadHtmlFile()

        // Si la pregunta no es anonima y el usuario no existe, redirigimos a la pagina de login
        if(!question.isAnonymous && usersRepository.userNotExists(userIP))
            call.respondRedirect(USER_ENDPOINT)

        // Si el tiempo para responder se ha agotado, redirigimos a la pagina de error
        if (questionRepository.getTimerState())
            call.respondRedirect("/error/5")

        // Si la pregunta no es de multiples respuestas y el usuario ya ha respondido, redirigimos a la pagina de error
        if (usersRepository.userResponded(userIP) && !question.isMultipleAnswers)
            call.respondRedirect("/error/7")

        try {
            call.respondText(
                text = fileContent!!,
                contentType = ContentType.Text.Html
            )
        } catch (e: Exception) { call.respondRedirect("/error/1") }
    }

    /**
     * Ruta que recibe y gestiona la respuesta del usuario.
     *
     * @return POST
     */
    private fun Route.handleSubmitQuestionRoute() = post("/submit") {
        val userIP = call.request.origin.remoteHost
        val choice = call.receiveParameters()["choice"]

        if (choice == "") return@post

        // Si aún no ha terminado el tiempo, se registra la respuesta
        if(!questionRepository.getTimerState())
            answerRegister(choice, userIP)

        call.respond(HttpStatusCode.OK)
    }

    /**
     * Ruta que muestra el formulario para acceder a la pregunta con nombre de usuario.
     *
     * @return GET
     */
    private fun Route.handleNewUserRoute() = get(USER_ENDPOINT) {
        // Pintamos el popup de error por si el usuario que introduce ya existe
        val fileContent = fileHandler.loadHtmlFile("login")

        try {
            call.respondText(
                text = fileContent!!,
                contentType = ContentType.Text.Html
            )
        } catch (e: Exception) { call.respondRedirect("/error/1") }
    }

    /**
     * Ruta que recibe y gestiona el nombre de usuario.
     *
     * @return POST
     */
    private fun Route.handleLoginRoute() = post("/login") {
        val userIP = call.request.origin.remoteHost
        val username = call.receiveParameters()["user"] ?: ""

        // Si ya hay un usuario con el mismo nombre, no se registra
        if (usersRepository.usernameExists(username)) {
            call.respond(HttpStatusCode.Conflict)
            return@post
        }

        // Si el usuario no existe, se registra
        if (usersRepository.userNotExists(userIP))
            usersRepository.addUser(User(userIP, username))

        call.respondRedirect(QUESTION_ENDPOINT)
    }

    private fun answerRegister(choice: String?, userIP: String) {
        // Si la respuesta no existe, se añade
        if (!questionRepository.answerExists(choice) && choice?.contains(";") == false)
            questionRepository.addAnswer(choice)

        questionRepository.incAnswerCount(choice)

        // Si el usuario no existe, se registra
        if (usersRepository.userNotExists(userIP))
            usersRepository.addUser(User(userIP, usersRepository.getUsernameByIp(userIP), true))
        else
            usersRepository.setUserResponded(userIP)

        if (choice != null)
            fileHandler.createDataFile(choice, userIP)
    }

}