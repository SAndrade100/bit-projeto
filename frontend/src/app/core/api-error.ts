import { HttpErrorResponse } from '@angular/common/http';
import { ProblemaApi } from './models';

/** Extrai uma mensagem amigável de qualquer erro HTTP. */
export function mensagemDeErro(erro: unknown): string {
  if (erro instanceof HttpErrorResponse) {
    if (erro.status === 0) {
      return 'Não foi possível conectar ao servidor. Verifique sua conexão.';
    }
    const problema = erro.error as ProblemaApi | null;
    if (problema?.detail) {
      return problema.detail;
    }
    if (erro.status === 403) {
      return 'Você não tem permissão para esta operação.';
    }
  }
  return 'Ocorreu um erro inesperado. Tente novamente.';
}

/** Erros por campo devolvidos pela API em respostas 400 de validação. */
export function errosDeCampos(erro: unknown): Record<string, string> {
  if (erro instanceof HttpErrorResponse && erro.status === 400) {
    return (erro.error as ProblemaApi | null)?.erros ?? {};
  }
  return {};
}
