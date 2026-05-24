<template>
  <MobileLayout class="overflow-hidden">
    <HeaderBar
      :isOfficial="false"
      :hidden-right="true"
      :enable-default-background="false"
      :enable-shadow="false"
      room-name="服务协议" />

    <div class="agreement-container">
      <div class="agreement-content">
        <div class="agreement-header">
          <h1>企业快书服务协议</h1>
          <p class="update-time">更新时间：2026年5月23日</p>
        </div>

        <div class="agreement-body">
          <section>
            <h2>1. 协议的接受与效力</h2>
            <p>
              欢迎使用企业快书！本《企业快书服务协议》（以下简称「本协议」）由您与企业快书运营方订立，适用于您下载、安装、注册、登录及使用企业快书客户端、管理端及相关服务的全部行为。
            </p>
            <p>
              请您在勾选同意或实际使用服务前，仔细阅读本协议全部条款，尤其是以加粗或下划线等方式提示的责任限制、免责及争议解决条款。您点击同意、完成注册或继续使用，即表示您已充分理解并接受本协议约束。
            </p>
            <p>
              若您代表企业或组织使用服务，您声明已获得该主体的合法授权，本协议对该主体同样具有约束力。
            </p>
          </section>

          <section>
            <h2>2. 服务内容与范围</h2>
            <p>
              企业快书面向企业及组织用户提供安全、高效的即时通讯与协作能力，包括但不限于单聊/群聊、文件传输、音视频通话、组织架构同步、消息漫游、多端登录及与企业信息系统对接等（具体以实际上线功能为准）。
            </p>
            <p>
              我们有权根据技术升级、合规要求或运营策略，对功能模块、服务形式、接口能力进行调整、优化或下线，并将通过合理方式向您提示；法律法规另有规定的除外。
            </p>
            <p>部分高级能力可能需单独开通或付费，相关规则以产品页面说明或另行签署的商业合同为准。</p>
          </section>

          <section>
            <h2>3. 账号注册与安全</h2>
            <p>3.1 您应使用真实、准确、完整且合法有效的信息完成注册或绑定，并及时更新；因信息不实导致的账号受限或损失，由您自行承担。</p>
            <p>
              3.2 账号、密码、验证码及绑定设备由您自行保管；凡通过您的账号凭证实施的操作，均视为您的行为，相关法律后果由您承担。发现账号被盗用或异常时，请立即通知我们或企业管理员。
            </p>
            <p>3.3 您同意接收与企业快书服务相关的系统通知、安全提醒、版本更新及依法必要的运营信息；营销类信息将按法律规定征得您的同意。</p>
          </section>

          <section>
            <h2>4. 用户行为规范</h2>
            <p>4.1 您在使用企业快书时，应遵守中华人民共和国法律法规、监管政策、行业规范及您所属企业的内部制度。</p>
            <p>4.2 不得利用本服务从事诈骗、赌博、传销、洗钱、侵犯国家安全、传播谣言或其他违法犯罪活动。</p>
            <p>
              4.3 不得发布、传输含有淫秽、暴力、恐怖、侮辱诽谤、侵犯他人名誉隐私、侵犯知识产权或其他违法违规内容的信息。
            </p>
            <p>4.4 不得未经授权访问、干扰、破坏服务器或网络，不得传播病毒、木马、恶意脚本或实施网络攻击。</p>
            <p>4.5 不得利用本服务批量发送垃圾信息、进行未授权的数据爬取，或以任何方式损害其他用户或第三方的合法权益。</p>
          </section>

          <section>
            <h2>5. 个人信息保护</h2>
            <p>
              5.1 我们重视用户个人信息保护，处理规则详见《企业快书隐私保护指引》。除法律法规另有规定或取得您另行同意外，我们不会向无关第三方披露您的个人信息。
            </p>
            <p>
              5.2 我们将采取合理的技术与管理措施，降低未经授权访问、泄露、篡改或丢失的风险；您亦应配合设置强密码并启用企业要求的安全策略。
            </p>
            <p>5.3 为向您提供、维护与改进服务，我们可能在必要范围内收集并使用设备信息、日志信息及您主动提交的内容；具体范围以隐私保护指引为准。</p>
          </section>

          <section>
            <h2>6. 知识产权</h2>
            <p>
              6.1 企业快书软件、界面设计、文档、商标、标识及相关技术的知识产权归运营方或权利人所有；未经书面许可，您不得复制、修改、传播或用于商业目的。
            </p>
            <p>6.2 未经书面授权，您不得对软件进行反向工程、反向汇编、反向编译，或以其他方式尝试获取源代码（法律明确允许的除外）。</p>
            <p>
              6.3 您通过企业快书上传、发送或存储的内容，其知识产权仍归您或原权利人所有；您授予运营方在全球范围内、免费的、非独占的许可，以便为提供、备份、展示及合规审查之目的使用该等内容。
            </p>
          </section>

          <section>
            <h2>7. 免责声明与责任限制</h2>
            <p>
              7.1 您理解并同意，在适用法律允许的最大范围内，因网络状况、通信线路、第三方服务、设备故障或不可抗力导致的服务中断、延迟或数据丢失，我们将尽力修复，但不承担超出法定范围的责任。
            </p>
            <p>
              7.2 企业快书按「现状」和「现有」基础提供服务，我们不对服务的持续性、无错误性或与您的特定商业目的完全匹配作出明示或默示保证。
            </p>
            <p>7.3 因您违反本协议、企业制度或法律法规而产生的投诉、索赔或行政处罚，由您自行承担；给我们或第三方造成损失的，您应负责赔偿。</p>
          </section>

          <section>
            <h2>8. 服务的变更、中断与终止</h2>
            <p>8.1 我们有权基于系统维护、安全加固、版本升级或业务调整，暂时中断或限制部分服务，并尽可能提前公告。</p>
            <p>8.2 若您严重违反本协议、从事违法活动或损害平台安全，我们有权采取警告、限制功能、冻结账号、终止服务等措施，并依法保留追究权利。</p>
            <p>8.3 您可按企业规则申请停用或注销账号；服务终止后，我们将依法删除或匿名化处理您的个人信息，法律法规另有规定的除外。</p>
          </section>

          <section>
            <h2>9. 协议的变更</h2>
            <p>
              我们有权根据法律法规变化或业务需要修订本协议。修订内容将通过应用内提示、官网公告或其他适当方式公布。若您不同意修订后的条款，应停止使用服务；您继续使用，即视为接受修订后的协议。
            </p>
          </section>

          <section>
            <h2>10. 法律适用与争议解决</h2>
            <p>10.1 本协议的订立、生效、履行、解释及争议解决，适用中华人民共和国大陆地区法律（冲突规范除外）。</p>
            <p>
              10.2 因本协议引起的或与本协议有关的争议，双方应友好协商；协商不成的，任何一方可向企业快书运营方住所地有管辖权的人民法院提起诉讼。
            </p>
          </section>

          <section>
            <h2>11. 第三方技术与服务说明</h2>
            <p>
              企业快书可能集成第三方 SDK、云服务、推送通道或地图等组件，以实现特定功能。相关服务由第三方依法提供，您使用时应同时遵守第三方条款。
            </p>
            <p>
              因第三方原因导致的功能异常、数据延迟或兼容性问题，我们将在合理范围内协助协调，但不就第三方自身过错承担连带责任（法律另有规定的除外）。
            </p>
            <p>我们可能对软件进行更新以修复缺陷或提升体验；建议您及时安装官方版本，因使用非官方或篡改版本导致的风险由您自行承担。</p>
          </section>

          <section>
            <h2>12. 其他</h2>
            <p>本协议条款部分无效或不可执行，不影响其余条款的效力。</p>
            <p>
              本协议未尽事宜，可参照隐私保护指引、产品说明或您与运营方另行签署的书面合同；冲突时，以对您权利限制更严格的专门约定为准（法律另有规定的除外）。
            </p>
            <p>本协议最终解释权归企业快书运营方在法律允许范围内行使。</p>
          </section>
        </div>
      </div>
    </div>
  </MobileLayout>
</template>

<script setup lang="ts"></script>

<style scoped lang="scss">
.agreement-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  user-select: none;
  -webkit-user-select: none;
}

.agreement-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;

  .agreement-header {
    text-align: center;
    margin-bottom: 30px;

    h1 {
      font-size: 24px;
      font-weight: bold;
      color: #333;
      margin-bottom: 10px;
    }

    .update-time {
      font-size: 14px;
      color: #666;
    }
  }

  .agreement-body {
    max-width: 800px;
    margin: 0 auto;
    line-height: 1.6;

    section {
      margin-bottom: 25px;

      h2 {
        font-size: 18px;
        font-weight: bold;
        color: #333;
        width: fit-content;
        margin-bottom: 15px;
        border-bottom: 2px solid var(--mobile-brand-accent);
        padding-bottom: 5px;
      }

      p {
        font-size: 14px;
        color: #555;
        margin-bottom: 10px;
        text-indent: 2em;
      }
    }
  }
}

.agreement-content::-webkit-scrollbar {
  width: 6px;
}

.agreement-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.agreement-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.agreement-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

:deep(.action-close) {
  svg {
    color: #303030;
  }
}
</style>
