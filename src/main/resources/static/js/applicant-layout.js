  // Sidebar toggle script to collapse/expand sidebar
  document.getElementById('menu-toggle')?.addEventListener('click', function(e) {
    e.preventDefault();
    document.getElementById('wrapper')?.classList.toggle('toggled');
});