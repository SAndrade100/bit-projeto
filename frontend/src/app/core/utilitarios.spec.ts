import { HttpErrorResponse } from '@angular/common/http';
import { errosDeCampos, mensagemDeErro } from './api-error';
import { paraIsoData } from './datas';

describe('mensagemDeErro / errosDeCampos', () => {
  it('usa o detalhe devolvido pela API', () => {
    const erro = new HttpErrorResponse({ status: 409, error: { detail: 'Apenas solicitações abertas podem ser excluídas.' } });
    expect(mensagemDeErro(erro)).toBe('Apenas solicitações abertas podem ser excluídas.');
  });

  it('trata falha de conexão e erros desconhecidos', () => {
    expect(mensagemDeErro(new HttpErrorResponse({ status: 0 }))).toContain('conectar');
    expect(mensagemDeErro(new Error('x'))).toContain('inesperado');
  });

  it('extrai erros por campo apenas de respostas 400', () => {
    const erro400 = new HttpErrorResponse({ status: 400, error: { erros: { titulo: 'Informe o título' } } });
    expect(errosDeCampos(erro400)).toEqual({ titulo: 'Informe o título' });
    expect(errosDeCampos(new HttpErrorResponse({ status: 500 }))).toEqual({});
  });
});

describe('paraIsoData', () => {
  it('formata a data local sem deslocar o dia', () => {
    expect(paraIsoData(new Date(2026, 9, 5, 23, 59))).toBe('2026-10-05');
    expect(paraIsoData(new Date(2026, 0, 1, 0, 0))).toBe('2026-01-01');
    expect(paraIsoData(null)).toBeNull();
  });
});
