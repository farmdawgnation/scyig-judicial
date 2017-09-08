(function() {
  window.judicialManager = {
    event: function(eventName, parameters) {
      var event = $.Event(eventName, parameters);
      $(document).trigger(event);
    }
  };

  var eventNames = {
    ajaxServicingStarted: "ajax-servicing-started",
    ajaxServicingEnded: "ajax-servicing-ended"
  };

  var apiUrls = {
    teamSuggestionUrl: function(competitionId) {
      return "/api/v1/competition/" + competitionId + "/team-suggestions";
    },
    judgeSuggestionUrl: function(competitionId) {
      return "/api/v1/competition/" + competitionId + "/judge-suggestions";
    }
  }

  $(document).on(eventNames.ajaxServicingStarted, function(event) {
    $("form").find("input, button, select").attr("disabled", "");
  });

  $(document).on(eventNames.ajaxServicingEnded, function(event) {
    $("form").find("input, button, select").removeAttr("disabled");
  });

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

  function suggest(urlFunc) {
    var competitionId = $(".schedule-configuration-form").data('competition-id');
    var url = urlFunc(competitionId);

    return function(partial, callback) {
      $.ajax({
        url: url,
        data: {
          q: partial
        },
        success: callback
      });
    }
  }

  window.bindSuggestions = function() {
    function reRenderAfterSelect(selection) {
      if (selection) {
        var data = $(".schedule-configuration-form").data('rerender-function-id') + "=_";
        var onFail = function () {
          alert("Rerender of scheduler failed");
        };
        lift.ajax(data, null, onFail);
      }
    }

    $(".match-row").each(function(index, rowElem) {
      $(rowElem).find(".prosecution-team").elemicaSuggest({
        suggestFunction: suggest(apiUrls.teamSuggestionUrl),
        valueInput: $(rowElem).find(".prosecution-team-id"),
        afterSelect: reRenderAfterSelect
      });

      $(rowElem).find(".defense-team").elemicaSuggest({
        suggestFunction: suggest(apiUrls.teamSuggestionUrl),
        valueInput: $(rowElem).find(".defense-team-id"),
        afterSelect: reRenderAfterSelect
      });

      $(rowElem).find(".presiding-judge").elemicaSuggest({
        suggestFunction: suggest(apiUrls.judgeSuggestionUrl),
        valueInput: $(rowElem).find(".presiding-judge-id"),
        afterSelect: reRenderAfterSelect
      });

      $(rowElem).find(".scoring-judge").elemicaSuggest({
        suggestFunction: suggest(apiUrls.judgeSuggestionUrl),
        valueInput: $(rowElem).find(".scoring-judge-id"),
        afterSelect: reRenderAfterSelect
      });
    });
  }
})();
