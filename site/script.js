(function () {
  var root = document.documentElement;
  var button = document.getElementById("themeToggle");
  if (button) {
    button.addEventListener("click", function () {
      var current = root.getAttribute("data-theme") ||
        (matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light");
      var next = current === "dark" ? "light" : "dark";
      root.setAttribute("data-theme", next);
      try { localStorage.setItem("theme", next); } catch (e) {}
    });
  }

  if (!matchMedia("(hover: hover)").matches) return;
  document.querySelectorAll(".card").forEach(function (card) {
    card.addEventListener("pointermove", function (e) {
      var r = card.getBoundingClientRect();
      card.style.setProperty("--mx", e.clientX - r.left + "px");
      card.style.setProperty("--my", e.clientY - r.top + "px");
    });
  });
})();
