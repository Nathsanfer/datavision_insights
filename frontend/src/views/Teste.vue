<script setup>
import * as XLSX from 'xlsx'
import { useUploadStore } from '../stores/uploadStore.js'

const upload = useUploadStore()

function aoSelecionaArquivo(event) {
  const file = event.target.files?.[0]
  if (!file) return

  upload.selecionarArquivo(file)
  upload.erros = []
}

async function processarArquivo() {
  if (!upload.arquivo) {
    upload.erros = ['Selecione uma planilha antes de processar.']
    return
  }

  const nome = upload.arquivo.name.toLowerCase()
  const valido = ['.xlsx', '.xls', '.csv'].some((ext) => nome.endsWith(ext))

  if (!valido) {
    upload.erros = ['Formato inválido. Envie .xlsx, .xls ou .csv.']
    return
  }

  upload.carregamento = true
  upload.erros = []

  try {
    const buffer = await upload.arquivo.arrayBuffer()
    const workbook = XLSX.read(buffer, { type: 'array' })
    const planilha = workbook.Sheets[workbook.SheetNames[0]]
    const linhas = XLSX.utils.sheet_to_json(planilha, { defval: '' })

    upload.dadosOriginais = linhas
    upload.dadosTratados = linhas.map((linha) => ({
      ...linha,
      codigo_cliente: linha.codigo_cliente || linha.codigo || linha.id || '-',
      nome_cliente: linha.nome_cliente || linha.cliente || linha.nome || 'Sem nome',
      segmento: upload.normalizarSegmento(linha.segmento),
      nivel_cliente: String(linha.nivel_cliente || linha.nivel || 'N/A').trim().toUpperCase()
    }))
  } catch (error) {
    upload.erros = ['Não foi possível ler a planilha.']
  } finally {
    upload.carregamento = false
  }
}
</script>

<template>
  <main class="max-w-5xl mx-auto p-6">
    <h1 class="text-2xl font-bold mb-4">Upload da Planilha</h1>

    <div class="mb-4">
      <input
        type="file"
        accept=".xlsx, .xls, .csv"
        @change="aoSelecionaArquivo"
        class="block w-full rounded border border-gray-300 p-2"
      />
    </div>

    <button
      type="button"
      @click="processarArquivo"
      :disabled="!upload.arquivo || upload.carregamento"
      class="mb-4 rounded bg-green-700 px-4 py-2 text-white disabled:cursor-not-allowed disabled:bg-gray-400"
    >
      {{ upload.carregamento ? 'Lendo planilha...' : 'Processar Planilha' }}
    </button>

    <p v-if="upload.carregamento" class="mb-4 text-blue-600">Lendo planilha...</p>
    <p v-if="upload.totalErros > 0" class="mb-4 text-red-600">{{ upload.totalErros }} erro(s) encontrado(s).</p>

    <div v-if="upload.arquivo" class="mb-4 text-sm text-gray-600">
      Arquivo selecionado: {{ upload.arquivo.name }}
    </div>

    <section v-if="upload.dadosTratados.length > 0" class="mt-6">
      <h2 class="text-xl font-semibold mb-3">Prévia dos dados</h2>
      <p>Total: {{ upload.totalClientes }}</p>
      <p>Clientes Nível A: {{ upload.clientesNivelA }}</p>

      <div class="mt-4 overflow-x-auto rounded border border-gray-200">
        <table class="min-w-full border-collapse text-left text-sm">
          <thead class="bg-gray-100">
            <tr>
              <th class="border-b border-gray-200 px-3 py-2">Código</th>
              <th class="border-b border-gray-200 px-3 py-2">Nome</th>
              <th class="border-b border-gray-200 px-3 py-2">Segmento</th>
              <th class="border-b border-gray-200 px-3 py-2">Nível</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(cliente, index) in upload.dadosTratados.slice(0, 10)" :key="index">
              <td class="border-b border-gray-200 px-3 py-2">{{ cliente.codigo_cliente || '-' }}</td>
              <td class="border-b border-gray-200 px-3 py-2">{{ cliente.nome_cliente || '-' }}</td>
              <td class="border-b border-gray-200 px-3 py-2">{{ cliente.segmento || '-' }}</td>
              <td class="border-b border-gray-200 px-3 py-2">{{ cliente.nivel_cliente || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </main>
</template>