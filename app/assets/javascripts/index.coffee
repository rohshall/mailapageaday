$ ->
  $.get "/listBooks", (data) ->
    $.each data, (index, item) ->
      $("#books").append "<li>Book with name: " + item.name + " stored in: " + item.filename + "</li>"
