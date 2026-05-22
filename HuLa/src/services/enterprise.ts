import { ImUrlEnum } from '@/enums'
import { imRequest } from '@/utils/ImRequestUtils'

export type EnterpriseProfile = {
  tenantId: number
  inviteCode: string
  tenantName: string
}

export async function fetchEnterpriseProfile() {
  return imRequest<EnterpriseProfile>({
    url: ImUrlEnum.ENTERPRISE_PROFILE
  })
}

export async function resolveEnterpriseCode(code: string) {
  return imRequest<EnterpriseProfile>({
    url: ImUrlEnum.RESOLVE_ENTERPRISE_CODE,
    params: { code }
  })
}
