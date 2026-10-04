import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  Categoria,
  Dashboard,
  FiltroSolicitacao,
  Pagina,
  Solicitacao,
  SolicitacaoForm,
  StatusSolicitacao,
} from './models';

@Injectable({ providedIn: 'root' })
export class SolicitacaoService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/solicitacoes';

  listar(filtro: FiltroSolicitacao, pagina: number, tamanho: number): Observable<Pagina<Solicitacao>> {
    let params = new HttpParams().set('pagina', pagina).set('tamanho', tamanho);
    const titulo = filtro.titulo?.trim();
    if (titulo) params = params.set('titulo', titulo);
    if (filtro.categoriaId) params = params.set('categoriaId', filtro.categoriaId);
    if (filtro.status) params = params.set('status', filtro.status);
    if (filtro.dataInicio) params = params.set('dataInicio', filtro.dataInicio);
    if (filtro.dataFim) params = params.set('dataFim', filtro.dataFim);
    return this.http.get<Pagina<Solicitacao>>(this.base, { params });
  }

  buscar(id: number): Observable<Solicitacao> {
    return this.http.get<Solicitacao>(`${this.base}/${id}`);
  }

  criar(dados: SolicitacaoForm): Observable<Solicitacao> {
    return this.http.post<Solicitacao>(this.base, dados);
  }

  atualizar(id: number, dados: SolicitacaoForm): Observable<Solicitacao> {
    return this.http.put<Solicitacao>(`${this.base}/${id}`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  alterarStatus(id: number, status: StatusSolicitacao): Observable<Solicitacao> {
    return this.http.patch<Solicitacao>(`${this.base}/${id}/status`, { status });
  }

  categorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>('/api/categorias');
  }

  dashboard(): Observable<Dashboard> {
    return this.http.get<Dashboard>('/api/dashboard');
  }
}
