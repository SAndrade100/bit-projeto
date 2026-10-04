export type StatusSolicitacao = 'ABERTO' | 'EM_ATENDIMENTO' | 'CONCLUIDO';

export const STATUS_OPCOES: { valor: StatusSolicitacao; rotulo: string }[] = [
  { valor: 'ABERTO', rotulo: 'Aberto' },
  { valor: 'EM_ATENDIMENTO', rotulo: 'Em Atendimento' },
  { valor: 'CONCLUIDO', rotulo: 'Concluído' },
];

export interface Usuario {
  id: number;
  username: string;
  nome: string;
}

export interface Categoria {
  id: number;
  nome: string;
}

export interface Solicitacao {
  id: number;
  codigo: string;
  titulo: string;
  descricao: string;
  categoria: Categoria;
  solicitante: Usuario;
  status: StatusSolicitacao;
  statusDescricao: string;
  /** Único status para o qual a solicitação pode avançar (nulo se já concluída). */
  proximoStatus: StatusSolicitacao | null;
  /** Verdadeiro se o usuário atual é o dono e a solicitação está aberta. */
  editavel: boolean;
  criadoEm: string;
  atualizadoEm: string;
}

export interface SolicitacaoForm {
  titulo: string;
  descricao: string;
  categoriaId: number;
}

export interface Pagina<T> {
  conteudo: T[];
  pagina: number;
  tamanho: number;
  totalElementos: number;
  totalPaginas: number;
}

export interface FiltroSolicitacao {
  titulo?: string;
  categoriaId?: number | null;
  status?: StatusSolicitacao | null;
  dataInicio?: string | null;
  dataFim?: string | null;
}

export interface Dashboard {
  total: number;
  abertas: number;
  emAtendimento: number;
  concluidas: number;
}

/** Corpo de erro da API (RFC 9457) com o mapa opcional de erros por campo. */
export interface ProblemaApi {
  title?: string;
  detail?: string;
  erros?: Record<string, string>;
}
