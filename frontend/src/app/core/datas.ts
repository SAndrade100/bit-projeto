/** Converte um Date local para "yyyy-MM-dd" sem passar por UTC (evita deslocar o dia). */
export function paraIsoData(data: Date | null): string | null {
  if (!data) return null;
  const mes = String(data.getMonth() + 1).padStart(2, '0');
  const dia = String(data.getDate()).padStart(2, '0');
  return `${data.getFullYear()}-${mes}-${dia}`;
}
