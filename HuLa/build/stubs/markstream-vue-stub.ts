import { defineComponent } from 'vue'

export const MarkdownCodeBlockNode = 'div'
export const setCustomComponents = () => {}
export const getUseMonaco = () => () => ({})
export default defineComponent({ name: 'MarkdownRenderStub', render: () => null })
