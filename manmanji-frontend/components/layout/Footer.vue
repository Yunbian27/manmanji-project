<!--
  Footer.vue — 页脚（Notion DESIGN.md 规范）
  深色背景 + 白色文字 + 三列网格
-->
<template>
  <footer class="footer">
    <div class="footer-grid">
      <div class="footer-col">
        <h4 class="footer-col-title">产品</h4>
        <a class="footer-link" href="#">慢慢记</a>
        <a class="footer-link" href="#">AI 助手</a>
        <a class="footer-link" href="#">Markdown 编辑器</a>
      </div>
      <div class="footer-col">
        <h4 class="footer-col-title">资源</h4>
        <a class="footer-link" href="#">使用文档</a>
        <a class="footer-link" href="#">API</a>
        <a class="footer-link" href="#">反馈</a>
      </div>
      <div class="footer-col">
        <h4 class="footer-col-title">关于</h4>
        <a class="footer-link" href="#">关于我们</a>
        <a class="footer-link" href="#">博客</a>
        <a class="footer-link" href="#">开源</a>
      </div>
    </div>
    <div class="footer-bottom">
      <span class="footer-copy">&copy; 2026 慢慢记</span>
      <a class="footer-icp" href="https://beian.miit.gov.cn/" target="_blank" rel="noopener">鄂ICP备2026025890号</a>
      <div class="footer-legal">
        <a class="footer-legal-link" href="#">隐私政策</a>
        <a class="footer-legal-link" href="#">服务条款</a>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  background: var(--primary);
  color: var(--on-dark);
  padding: var(--spacing-xxl) var(--spacing-xxl);
}

.footer-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-xl);
  max-width: 1000px;
  margin: 0 auto;
}

.footer-col {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.footer-col-title {
  font-size: var(--body-md);
  font-weight: var(--weight-medium);
  color: var(--on-dark);
  margin: 0 0 var(--spacing-xs) 0;
}

.footer-link {
  font-size: var(--body-sm);
  font-weight: var(--weight-regular);
  color: var(--stone);
  text-decoration: none;
  transition: color 0.15s var(--ease);
}
.footer-link:hover { color: var(--on-dark); }

.footer-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1000px;
  margin: var(--spacing-xl) auto 0;
  padding-top: var(--spacing-md);
  border-top: 1px solid rgba(255, 255, 255, 0.15);
}

.footer-copy {
  font-size: var(--caption);
  font-weight: var(--weight-regular);
  color: var(--muted);
}

.footer-icp {
  font-size: var(--caption);
  font-weight: var(--weight-regular);
  color: var(--muted);
  text-decoration: none;
  margin-left: var(--spacing-md);
}
.footer-icp:hover { color: var(--on-dark); }

.footer-legal {
  display: flex;
  gap: var(--spacing-md);
}

.footer-legal-link {
  font-size: var(--body-sm);
  font-weight: var(--weight-regular);
  color: var(--link-blue);
  text-decoration: underline;
}
.footer-legal-link:hover { opacity: 0.8; }

@media (max-width: 599px) {
  .footer { padding: var(--spacing-md); }
  .footer-grid { grid-template-columns: 1fr; gap: var(--spacing-md); }
  .footer-bottom { flex-direction: column; gap: var(--spacing-xs); text-align: center; }
}
</style>
