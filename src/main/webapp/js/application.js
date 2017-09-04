(function() {
  $("#select-competition-container").on('click', '.competition-entry', function(event) {
    var $target = $(event.target);

    if (! $target.is(".select-competition")) {
      var href = $(event.target)
        .closest(".competition-entry")
        .find(".select-competition")
        .attr('href');

      window.location.href = href;
    }
  });
})();