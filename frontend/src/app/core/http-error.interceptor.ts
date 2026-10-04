import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

/**
 * Se a API responder 401 em qualquer chamada (exceto as de autenticação, tratadas pela própria tela),
 * a sessão expirou: limpa o estado e leva o usuário ao login.
 */
export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((erro: unknown) => {
      if (erro instanceof HttpErrorResponse && erro.status === 401 && !req.url.startsWith('/api/auth/')) {
        auth.limparSessao();
        router.navigate(['/login'], { queryParams: { redirect: router.url, expirada: 1 } });
      }
      return throwError(() => erro);
    }),
  );
};
