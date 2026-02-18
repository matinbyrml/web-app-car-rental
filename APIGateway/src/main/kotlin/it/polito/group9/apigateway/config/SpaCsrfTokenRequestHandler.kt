package it.polito.group9.apigateway.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.util.function.Supplier
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler
import org.springframework.security.web.csrf.CsrfTokenRequestHandler
import org.springframework.util.StringUtils

class SpaCsrfTokenRequestHandler : CsrfTokenRequestAttributeHandler() {
  private val delegate: CsrfTokenRequestHandler = CsrfTokenRequestAttributeHandler()

  override fun handle(req: HttpServletRequest, res: HttpServletResponse, t: Supplier<CsrfToken>) {
    delegate.handle(req, res, t)
  }

  override fun resolveCsrfTokenValue(request: HttpServletRequest, csrfToken: CsrfToken): String? {
    return if (StringUtils.hasText(request.getHeader(csrfToken.headerName))) {
      super.resolveCsrfTokenValue(request, csrfToken)
    } else {
      delegate.resolveCsrfTokenValue(request, csrfToken)
    }
  }
}
