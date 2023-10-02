// mock function to be replaced with external API request

export function fetchUsername(name = "undefined") {
  return new Promise<{ data: string }>((resolve) =>
    setTimeout(() => resolve({ data: name }), 500)
  );
}