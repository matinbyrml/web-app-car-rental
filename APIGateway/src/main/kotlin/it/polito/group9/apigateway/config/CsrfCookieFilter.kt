package it.polito.group9.apigateway.config

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.io.IOException
import kotlin.jvm.Throws
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.filter.OncePerRequestFilter

class CsrfCookieFilter : OncePerRequestFilter() {
  @Throws(ServletException::class, IOException::class)
  override fun doFilterInternal(
      request: HttpServletRequest,
      response: HttpServletResponse,
      filterChain: FilterChain
  ) {
    val csrfToken = request.getAttribute("_csrf") as CsrfToken

    response.addHeader(csrfToken.headerName, csrfToken.token)

    filterChain.doFilter(request, response)
  }
}
