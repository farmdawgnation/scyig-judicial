// Generated by CoffeeScript 1.9.2

/*
elemicaSuggest 0.9.2-SNAPSHOT
(c)2014 Elemica - Licensed under the terms of the Apache 2.0 License.
 */

(function() {
  var indexOf = [].indexOf || function(item) { for (var i = 0, l = this.length; i < l; i++) { if (i in this && this[i] === item) return i; } return -1; };

  (function($) {
    var noop;
    noop = function() {};
    return $.fn.extend({
      elemicaSuggest: function(options) {
        var $valueInput, ALT, BACKSPACE, CTRL, DOWN_ARROW, ENTER, KEY_N, KEY_P, NON_PRINTALBE_KEYS, OS_LEFT, OS_RIGHT, SHIFT, TAB, UP_ARROW, afterSelect, afterSuggest, buildMarkerRegExp, currentHighlightedDisplayText, highlightAnother, highlightNext, highlightPrevious, isSelectingSuggestion, markMatches, minimumSearchTermLength, noMatchesMessage, noSuggestionMatched, populateSuggestions, removeSuggestions, selectHighlighted, selectionIndicatorTarget, suggestFunction;
        if (options == null) {
          options = {};
        }
        KEY_N = 78;
        KEY_P = 80;
        UP_ARROW = 38;
        DOWN_ARROW = 40;
        ENTER = 13;
        TAB = 9;
        BACKSPACE = 8;
        SHIFT = 16;
        CTRL = 17;
        ALT = 18;
        OS_LEFT = 91;
        OS_RIGHT = 92;
        NON_PRINTALBE_KEYS = [SHIFT, CTRL, ALT, OS_LEFT, OS_RIGHT, TAB];
        isSelectingSuggestion = function() {
          return $(".suggestions").is(":visible");
        };
        suggestFunction = options.suggestFunction || function(term, _) {
          return typeof console !== "undefined" && console !== null ? console.warn("No suggest function defined.") : void 0;
        };
        minimumSearchTermLength = options.minimumSearchTermLength != null ? options.minimumSearchTermLength : 2;
        $valueInput = options.valueInput || $("<input />");
        selectionIndicatorTarget = options.selectionIndicatorTarget || function($target) {
          return $target;
        };
        noMatchesMessage = options.noMatchesMessage || $(this.first()).data('no-matches');
        afterSuggest = options.afterSuggest || noop;
        afterSelect = options.afterSelect || noop;
        noSuggestionMatched = options.noSuggestionMatched || function() {
          return true;
        };
        buildMarkerRegExp = options.buildMarkerRegExp || noop;
        removeSuggestions = function(element) {
          return $(element).siblings(".suggestions").remove();
        };
        highlightAnother = function(element, otherCalcFunc) {
          var $currentActive, $nextElement;
          $currentActive = $(element).parent().find(".suggestions > .active");
          $nextElement = otherCalcFunc($currentActive);
          if ($currentActive.length && $nextElement.length) {
            $currentActive.removeClass("active");
            return $nextElement.addClass("active");
          } else if (!$currentActive.length) {
            return $(element).parent().find(".suggestions > li:first-child").addClass("active");
          }
        };
        highlightNext = function(element) {
          return highlightAnother(element, function($currentActive) {
            return $currentActive.next();
          });
        };
        highlightPrevious = function(element) {
          return highlightAnother(element, function($currentActive) {
            return $currentActive.prev();
          });
        };
        selectHighlighted = function(element) {
          $(element).parent().find(".suggestions .active").trigger("element-selected").end().end().trigger("focus");
          removeSuggestions(element);
          return selectionIndicatorTarget($(element)).addClass("has-selection");
        };
        currentHighlightedDisplayText = function(element) {
          return $(element).parent().find(".suggestions > .active").text();
        };
        markMatches = function(markerRegExp) {
          return function(textToMark) {
            var currentIndex, i, latestMatch, markedContent, matches, prefix;
            markedContent = [];
            currentIndex = 0;
            while (latestMatch = markerRegExp.exec(textToMark)) {
              if (latestMatch[0].length === 0) {
                break;
              }
              prefix = textToMark.substring(currentIndex, latestMatch.index);
              matches = (function() {
                var j, ref, results;
                results = [];
                for (i = j = 1, ref = latestMatch.length; 1 <= ref ? j < ref : j > ref; i = 1 <= ref ? ++j : --j) {
                  results.push($('<span />').addClass('match').text(latestMatch[i]));
                }
                return results;
              })();
              currentIndex = latestMatch.index + Math.max(latestMatch[0].length, 1);
              markedContent.push(document.createTextNode(prefix));
              markedContent.push.apply(markedContent, matches);
              if (!markerRegExp.global) {
                break;
              }
            }
            markedContent.push(document.createTextNode(textToMark.substring(currentIndex)));
            return markedContent;
          };
        };
        populateSuggestions = function(element, markMatchRegExp) {
          return function(suggestions) {
            var $suggestionsList, matchMarker, suggestion;
            $suggestionsList = $(element).siblings(".suggestions");
            if ($suggestionsList.length === 0) {
              $suggestionsList = $("<ul />").addClass("suggestions");
              $(element).parent().append($suggestionsList);
            }
            matchMarker = markMatchRegExp != null ? markMatches(markMatchRegExp) : void 0;
            $suggestionsList.empty().append((function() {
              var j, len, results;
              results = [];
              for (j = 0, len = suggestions.length; j < len; j++) {
                suggestion = suggestions[j];
                results.push((function(suggestion) {
                  var $suggestionLi;
                  $suggestionLi = $("<li />");
                  if (matchMarker != null) {
                    $suggestionLi.append(matchMarker(suggestion.display));
                  } else {
                    $suggestionLi.text(suggestion.display);
                  }
                  $suggestionLi.on('mousedown element-selected', function() {
                    $(element).val(suggestion.display);
                    $valueInput.val(suggestion.value);
                    return afterSelect(suggestion);
                  }).on('mouseover', function() {
                    return $(this).siblings().removeClass("active").end().addClass("active");
                  });
                  if (suggestion.image != null) {
                    $suggestionLi.prepend($("<img />").attr("src", suggestion.image));
                  }
                  if (suggestion.metadata != null) {
                    $suggestionLi.append($("<span />").text(suggestion.metadata).addClass("metadata"));
                  }
                  return $suggestionLi;
                })(suggestion));
              }
              return results;
            })()).find("li:first-child").addClass("active");
            if (suggestions.length === 0) {
              $suggestionsList.append($("<li />").text(noMatchesMessage).addClass("invalid-text"));
            }
            return afterSuggest();
          };
        };
        return this.each(function() {
          $(this).attr('autocomplete', 'off');
          $(this).on('blur', function(event) {
            var $target, originalValue;
            $target = $(event.target);
            if (isSelectingSuggestion() && $target.val().toLowerCase() === currentHighlightedDisplayText(this).toLowerCase()) {
              selectHighlighted(this);
            } else {
              removeSuggestions(this);
            }
            if ($valueInput.val() === "") {
              if (noSuggestionMatched($target.val(), afterSelect)) {
                originalValue = $target.val();
                $target.val("");
                if (originalValue !== "") {
                  return afterSelect(null);
                }
              }
            }
          });
          $(this).on('keydown', (function(_this) {
            return function(event) {
              var ctrlPressed, key;
              key = event.which;
              ctrlPressed = event.ctrlKey;
              if (key === UP_ARROW || key === DOWN_ARROW || (ctrlPressed && (key === KEY_P || key === KEY_N))) {
                return event.preventDefault();
              } else if (event.which === ENTER && isSelectingSuggestion()) {
                return event.preventDefault();
              } else if (event.which === TAB && isSelectingSuggestion()) {
                return selectHighlighted(_this);
              } else if (event.which === BACKSPACE && $valueInput.val() !== "") {
                $valueInput.val("");
                $(event.target).val("");
                return afterSelect(null);
              }
            };
          })(this));
          return $(this).on('keyup', (function(_this) {
            return function(event) {
              var $target, ctrlPressed, key, markMatchRegExp, searchTerm;
              key = event.which;
              ctrlPressed = event.ctrlKey;
              switch (false) {
                case !(key === UP_ARROW || (ctrlPressed && key === KEY_P)):
                  return highlightPrevious(_this);
                case !(key === DOWN_ARROW || (ctrlPressed && key === KEY_N)):
                  return highlightNext(_this);
                case key !== ENTER:
                  return selectHighlighted(_this);
                case indexOf.call(NON_PRINTALBE_KEYS, key) >= 0:
                  $valueInput.val("");
                  $target = $(event.target);
                  selectionIndicatorTarget($target).removeClass("has-selection");
                  searchTerm = $.trim($target.val());
                  if (searchTerm.length >= minimumSearchTermLength) {
                    markMatchRegExp = buildMarkerRegExp(searchTerm);
                    return suggestFunction(searchTerm, populateSuggestions(_this, markMatchRegExp));
                  } else {
                    return removeSuggestions(_this);
                  }
              }
            };
          })(this));
        });
      }
    });
  })(jQuery);

}).call(this);
