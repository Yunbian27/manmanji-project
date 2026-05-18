<!--
  Footer.vue — 页脚（Uber 风格）
  黑色背景 + 白色文字 + 三列网格
  BLUEPRINT 7.5
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
      <div class="footer-legal">
        <a class="footer-legal-link" href="#">隐私政策</a>
        <a class="footer-legal-link" href="#">服务条款</a>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  background: var(--primary);           /* #000000 */
  color: var(--on-dark);                /* #ffffff */
  padding: var(--space-3xl) var(--space-3xl); /* 32px */
}

.footer-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-2xl);
  max-width: var(--content-max);
  margin: 0 auto;
}

.footer-col {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}

.footer-col-title {
  font-size: var(--text-body-md-strong); /* 16px */
  font-weight: var(--weight-medium);     /* 500 */
  color: var(--on-dark);
  margin: 0 0 var(--space-xs) 0;
}

.footer-link {
  font-size: var(--text-body-sm);       /* 14px */
  font-weight: var(--weight-regular);   /* 400 */
  color: var(--muted-soft);             /* #d1d5db — 近似 hairline-mid */
  text-decoration: none;
  transition: color 0.15s ease;
}
.footer-link:hover { color: var(--on-dark); }

.footer-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: var(--content-max);
  margin: var(--space-2xl) auto 0;
  padding-top: var(--space-lg);
  border-top: 1px solid var(--hairline-mid);
}

.footer-copy {
  font-size: var(--text-caption);       /* 12px */
  font-weight: var(--weight-regular);
  color: var(--muted);                  /* #afafaf */
}

.footer-legal {
  display: flex;
  gap: var(--space-lg);
}

.footer-legal-link {
  font-size: var(--text-body-sm);       /* 14px */
  font-weight: var(--weight-regular);
  color: var(--link);                   /* #0000ee */
  text-decoration: underline;
}
.footer-legal-link:hover { opacity: 0.8; }

@media (max-width: 599px) {
  .footer { padding: var(--space-lg); }
  .footer-grid { grid-template-columns: 1fr; gap: var(--space-lg); }
  .footer-bottom { flex-direction: column; gap: var(--space-sm); text-align: center; }
}
</style>
